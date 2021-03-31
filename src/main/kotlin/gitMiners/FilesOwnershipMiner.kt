package gitMiners

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.EditList
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.util.io.DisabledOutputStream
import util.ProjectConfig
import util.UtilFunctions
import util.serialization.ConcurrentHashMapSerializer
import java.io.File
import java.util.*
import java.util.concurrent.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.pow

class FilesOwnershipMiner(
    repository: FileRepository,
    private val neededBranch: String = ProjectConfig.DEFAULT_BRANCH,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) : GitMiner(repository, setOf(neededBranch), numThreads = numThreads) {

    private val threadLocalDiffFormatter = object : ThreadLocal<DiffFormatter>() {
        override fun initialValue(): DiffFormatter {
            val diffFormatter = DiffFormatter(DisabledOutputStream.INSTANCE)
            diffFormatter.setRepository(repository)
            diffFormatter.setDiffComparator(RawTextComparator.DEFAULT)
            diffFormatter.isDetectRenames = true
            return diffFormatter
        }
    }

    private val serializer = ConcurrentHashMapSerializer(
        Int.serializer(),
        ConcurrentHashMapSerializer(Int.serializer(), UserData.serializer())
    )

    // [fileId][userId]
    private val filesOwnership: ConcurrentHashMap<Int, ConcurrentHashMap<Int, UserData>> = ConcurrentHashMap()

    // [fileId][line] = set(userId, ...)
    private val authorsForLine: ConcurrentHashMap<Int, ConcurrentHashMap<Int, ConcurrentSkipListSet<Int>>> =
        ConcurrentHashMap()

    // denoting the total potential authorship amount of all developers
    // [fileId]
    private val potentialAuthorship = mutableMapOf<Int, Int>()

    // denoting the total potential authorship amount of all developers
    // [userId][fileId]
    private val developerKnowledge: HashMap<Int, HashMap<Int, Double>> = HashMap()

    private val decayFactor = 0.01

    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {}

    data class FutureResult(val list: List<Pair<EditList, Int>>, val decay: Double, val userId: Int) {
        constructor() : this(emptyList(), 1.0, -1)
    }

    @Serializable
    class UserData {
        private val ownedLines = mutableSetOf<Int>()
        var authorship: Double = 0.0
            private set

        fun calculateAuthorship(lines: IntRange, decay: Double) {
            val newLines = lines - ownedLines
            if (newLines.isNotEmpty()) {
                authorship += newLines.size * decay
                ownedLines += newLines
            }
        }

    }

    private fun getListOfFutures(
        commitsInBranch: List<RevCommit>,
        latestCommitDate: Date,
        threadPool: ExecutorService
    ): List<Future<FutureResult>> {

        val futures = ArrayList<Future<FutureResult>>()
        for ((currCommit, prevCommit) in commitsInBranch.windowed(2)) {
            if (!addProceedCommits(currCommit, prevCommit)) continue

            val callable = Callable {
                try {
                    val git = threadLocalGit.get()
                    val reader = threadLocalReader.get()
                    val diffFormatter = threadLocalDiffFormatter.get()

                    val diffs = reader.use { UtilGitMiner.getDiffsWithoutText(currCommit, prevCommit, it, git) }
                    val email = currCommit.authorIdent.emailAddress

                    val userId = userMapper.add(email)
                    val date = currCommit.authorIdent.getWhen()
                    val diffDays: Int = TimeUnit.DAYS.convert(
                        latestCommitDate.time - date.time,
                        TimeUnit.MILLISECONDS
                    )
                        .toInt()

                    val decay = (1 - decayFactor).pow(diffDays)

                    val list = mutableListOf<Pair<EditList, Int>>()
                    for (diff in diffs) {
                        val editList = diffFormatter.toFileHeader(diff).toEditList()
                        val fileId = fileMapper.add(diff.oldPath)
                        list.add(editList to fileId)
                    }

                    FutureResult(list, decay, userId)
                } catch (e: Exception) {
                    e.printStackTrace()
                    FutureResult()
                }
            }

            val future = threadPool.submit(callable)
            futures.add(future)
        }

        return futures
    }

    override fun run() {
        val git = threadLocalGit.get()
        val branch = UtilGitMiner.findNeededBranchOrNull(git, neededBranch) ?: return

        val commitsInBranch = getUnprocessedCommits(branch.name)
        val commitsPairsCount = commitsInBranch.size - 1
        if (commitsPairsCount == 0 || commitsPairsCount == -1) {
            println("Nothing to proceed in branch $branch")
            return
        }

        val threadPool = Executors.newFixedThreadPool(numThreads)

        val latestCommit = commitsInBranch.iterator().next()
        val latestCommitDate = latestCommit.authorIdent.getWhen()

        processLatestCommit(latestCommit, threadPool)

        val futures = getListOfFutures(commitsInBranch, latestCommitDate, threadPool)
        var num = 0
        for (future in futures) {
            val (listsToFileId, decay, userId) = future.get()
            for ((editList, fileId) in listsToFileId) {
                for (edit in editList) {
                    // TODO: what about deleted lines?
                    calculateAuthorshipForLines(edit.beginB..edit.endB, fileId, userId, decay)
                    addAuthorsForLines(edit.beginB..edit.endB, fileId, userId)
                }
            }

            if (num % logFrequency == 0 || num == futures.size) {
                println("Processed $num commits of ${futures.size}")
            }
            num++

        }

        calculatePotentialAuthorship()
        calculateDeveloperKnowledge()

        threadPool.shutdown()
    }

    private fun processLatestCommit(latestCommit: RevCommit, threadPool: ExecutorService) {
        println("Start processing latest commit")
        val treeWalk = TreeWalk(repository)
        treeWalk.addTree(latestCommit.tree)
        treeWalk.isRecursive = false

        val idToFileList = mutableListOf<Pair<Int, String>>()

        while (treeWalk.next()) {
            if (treeWalk.isSubtree) {
                treeWalk.enterSubtree()
                continue
            }
            val filePath = treeWalk.pathString
            val fileId = fileMapper.add(filePath)
            idToFileList.add(fileId to filePath)
        }

        if (idToFileList.size == 0) return

        val latch = CountDownLatch(idToFileList.size)

        for ((fileId, filePath) in idToFileList) {
            threadPool.execute {
                try {
                    val git = threadLocalGit.get()

                    val blameResult = git
                        .blame()
                        .setFilePath(filePath)
                        .setTextComparator(RawTextComparator.WS_IGNORE_ALL)
                        .setStartCommit(latestCommit)
                        .call()

                    val rawText = blameResult.resultContents

                    for (lineNumber in 0 until rawText.size()) {
                        val sourceAuthor = blameResult.getSourceAuthor(lineNumber)
                        val userId = userMapper.add(sourceAuthor.emailAddress)

                        // Each file only one time
                        calculateAuthorshipForLines(lineNumber..lineNumber, fileId, userId, 1.0)
                        addAuthorsForLines(lineNumber..lineNumber, fileId, userId)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    latch.countDown()
                }
            }

        }

        latch.await()
        println("End processing latest commit")
    }

    override fun saveToJson(resourceDirectory: File) {
        UtilFunctions.saveToJson(
            File(resourceDirectory, ProjectConfig.FILES_OWNERSHIP),
            filesOwnership, serializer
        )
        UtilFunctions.saveToJson(File(resourceDirectory, ProjectConfig.POTENTIAL_OWNERSHIP), potentialAuthorship)
        UtilFunctions.saveToJson(File(resourceDirectory, ProjectConfig.DEVELOPER_KNOWLEDGE), developerKnowledge)
        saveMappers(resourceDirectory)
    }

    private fun addAuthorsForLines(lines: IntRange, fileId: Int, userId: Int) {
        authorsForLine
            .computeIfAbsent(fileId) { ConcurrentHashMap() }
            .computeIfAbsent(userId) { ConcurrentSkipListSet() }
            .addAll(lines)
    }

    private fun calculateAuthorshipForLines(lines: IntRange, fileId: Int, userId: Int, decay: Double) {
        filesOwnership
            .computeIfAbsent(fileId) { ConcurrentHashMap() }
            .computeIfAbsent(userId) { UserData() }
            .calculateAuthorship(lines, decay)
    }

    private fun calculatePotentialAuthorship() {
        println("Start calculating potential authorship")
        for (fileEntry in authorsForLine) {
            val fileId = fileEntry.key
            var potentialAuthorshipForFile = 0
            for (lineEntry in fileEntry.value) {
                potentialAuthorshipForFile += lineEntry.value.size
            }
            potentialAuthorship[fileId] = potentialAuthorshipForFile
        }
        println("End calculating potential authorship")
    }

    private fun calculateDeveloperKnowledge() {
        println("Start calculating developer knowledge")
        for (entryOwnership in filesOwnership) {
            val fileId = entryOwnership.key
            for (entryUserData in entryOwnership.value) {
                val userId = entryUserData.key
                val userData = entryUserData.value
                developerKnowledge
                    .computeIfAbsent(userId) { HashMap() }[fileId] =
                    userData.authorship / potentialAuthorship[fileId]!!
            }
        }
        println("End calculating developer knowledge")
    }


}
