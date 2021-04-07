package gitMiners

import dataProcessor.FilesOwnershipDataProcessor
import dataProcessor.FilesOwnershipDataProcessor.*
import gitMiners.exceptions.ProcessInThreadPoolException
import org.eclipse.jgit.diff.EditList
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevCommit
import util.ProjectConfig
import util.UtilFunctions
import java.util.*
import java.util.concurrent.*
import kotlin.collections.ArrayList

class FilesOwnershipMiner(
    repository: FileRepository,
    private val neededBranch: String = ProjectConfig.DEFAULT_BRANCH,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) : GitMinerNew<FilesOwnershipDataProcessor>(repository, setOf(neededBranch), numThreads = numThreads) {

    private data class FutureResult(
        val listOfEditsToFile: List<Pair<EditList, String>>,
        val commitDate: Date,
        val user: String
    )

    private fun getListOfFutures(
        commitsInBranch: List<RevCommit>,
        threadPool: ExecutorService
    ): List<Future<FutureResult>> {
        // TODO: Refactor
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

                    val commitDate = currCommit.authorIdent.getWhen()


                    val list = mutableListOf<Pair<EditList, String>>()
                    for (diff in diffs) {
                        val editList = diffFormatter.toFileHeader(diff).toEditList()
                        val filePath = diff.oldPath
                        list.add(editList to filePath)
                    }

                    FutureResult(list, commitDate, email)
                } catch (e: Exception) {
                    val msg = "Got error while processing commits ${currCommit.name} and ${prevCommit.name}"
                    throw ProcessInThreadPoolException(msg, e)
                }
            }

            futures.add(threadPool.submit(callable))
        }

        return futures
    }

    override fun process(dataProcessor: FilesOwnershipDataProcessor, currCommit: RevCommit, prevCommit: RevCommit) {}

    override fun run(dataProcessor: FilesOwnershipDataProcessor) {
        val git = threadLocalGit.get()
        val branch = UtilGitMiner.findNeededBranch(git, neededBranch)

        // TODO: Refactor, code
        val commitsInBranch = getUnprocessedCommits(branch.name)
        val commitsPairsCount = commitsInBranch.size - 1
        if (commitsPairsCount == 0 || commitsPairsCount == -1) {
            println("Nothing to proceed in branch $branch")
            return
        }

        val latestCommit = commitsInBranch.iterator().next()
        val latestCommitDate = latestCommit.authorIdent.getWhen()

        val threadPool = Executors.newFixedThreadPool(numThreads)
        val initEntityList = processLatestCommit(latestCommit, threadPool)
        val initData = InitData(latestCommitDate, initEntityList)
        dataProcessor.init(initData)

        val futures = getListOfFutures(commitsInBranch, threadPool)
        var num = 0
        for (future in futures) {

            val (listsToFileId, diffDays, userId) = future.get()

            for ((editList, fileId) in listsToFileId) {
                for (edit in editList) {
                    val data = FileLinesAddedByUser(edit.beginB..edit.endB, fileId, userId, diffDays)
                    dataProcessor.processData(data)
                }
            }

            if (num % logFrequency == 0 || num == futures.size) {
                println("Processed $num commits of ${futures.size}")
            }
            num++

        }

        dataProcessor.calculate()

        threadPool.shutdown()
    }

    private fun processLatestCommit(latestCommit: RevCommit, threadPool: ExecutorService): List<FileLineOwnedByUser> {
        println("Start processing latest commit")

        val filePaths = UtilGitMiner.getAllFilePathsOnCommit(repository, latestCommit)

        val concurrentLinkedQueue = ConcurrentLinkedQueue<FileLineOwnedByUser>()

        val tasks = mutableListOf<Runnable>()
        for (filePath in filePaths) {

            val runnable = Runnable {
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
                        val user = sourceAuthor.emailAddress
                        concurrentLinkedQueue.add(FileLineOwnedByUser(lineNumber, filePath, user))
                    }
                } catch (e: Exception) {
                    val msg =
                        "Got error while processing latest commits file tree. Commit: ${latestCommit.name}, File: $filePath"
                    throw ProcessInThreadPoolException(msg, e)
                }
            }

            tasks.add(runnable)

        }

        UtilFunctions.runInThreadPoolWithExceptionHandle(threadPool, tasks)

        println("End processing latest commit")

        return concurrentLinkedQueue.toList()
    }

}
