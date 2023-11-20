package miners.gitMiners

import dataProcessor.FilesOwnershipDataProcessor
import dataProcessor.initData.LatestCommitOwnedLines
import dataProcessor.initData.entity.FileLineOwnedByUser
import dataProcessor.inputData.FileLinesAddedByUser
import miners.gitMiners.GitMinerUtil.isNotNeededFilePath
import miners.gitMiners.exceptions.ProcessInThreadPoolException
import org.eclipse.jgit.diff.EditList
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.revwalk.RevCommit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import util.HelpFunctionsUtil
import util.ProjectConfig
import java.io.File
import java.util.*
import java.util.concurrent.*

class FilesOwnershipMiner(
    repositoryFile: File,
    val neededBranch: String,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS,
    val filesToProceed: Set<String>? = null
) : GitMiner<FilesOwnershipDataProcessor>(repositoryFile, setOf(neededBranch), numThreads = numThreads) {

    private val log: Logger = LoggerFactory.getLogger(FilesOwnershipMiner::class.java)
    
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
        for (commit in commitsInBranch) {
            if (!addProceedCommits(commit)) continue

            val callable = Callable {
                try {
                    val reader = threadLocalReader.get()
                    val diffFormatter = threadLocalDiffFormatter.get()

                    val diffs = reader.use { GitMinerUtil.getDiffsWithoutText(commit, it, repository) }
                    val email = commit.authorIdent.emailAddress

                    val commitDate = commit.authorIdent.getWhen()


                    val list = mutableListOf<Pair<EditList, String>>()
                    for (diff in diffs) {
                        val filePath = GitMinerUtil.getFilePath(diff)
                        if (isNotNeededFilePath(filePath, filesToProceed)) continue

                        val editList = diffFormatter.toFileHeader(diff).toEditList()
                        list.add(editList to filePath)
                    }

                    FutureResult(list, commitDate, email)
                } catch (e: Exception) {
                    val msg = "Got error while processing commit ${commit.name}"
                    throw ProcessInThreadPoolException(msg, e)
                }
            }

            futures.add(threadPool.submit(callable))
        }

        return futures
    }

    override fun process(dataProcessor: FilesOwnershipDataProcessor, commit: RevCommit) {}

    override fun run(dataProcessor: FilesOwnershipDataProcessor) {
        val git = threadLocalGit.get()
        val branch = GitMinerUtil.findNeededBranch(git, neededBranch)

        // TODO: Refactor, code
        val commitsInBranch = getUnprocessedCommits(branch.name)
        if (commitsInBranch.isEmpty()) {
            log.info("Nothing to proceed in branch $branch")
            return
        }

        val latestCommit = commitsInBranch.first()
        val latestCommitDate = latestCommit.authorIdent.getWhen()

        val threadPool = Executors.newFixedThreadPool(numThreads)
        val initEntityList = processLatestCommit(latestCommit, threadPool)
        val initData = LatestCommitOwnedLines(latestCommitDate, initEntityList)
        dataProcessor.init(initData)

        val futures = getListOfFutures(commitsInBranch, threadPool)
        var num = 0
        for (future in futures) {

            val (listsToFileId, commitDate, user) = future.get()

            for ((editList, filePath) in listsToFileId) {
                for (edit in editList) {
                    val data = FileLinesAddedByUser(edit.beginB..edit.endB, filePath, user, commitDate)
                    dataProcessor.processData(data)
                }
            }

            if (num % logFrequency == 0 || num == futures.size) {
                log.info("Processed $num commits out of ${futures.size}")
            }
            num++

        }

        dataProcessor.calculate()

        threadPool.shutdown()
    }

    private fun processLatestCommit(latestCommit: RevCommit, threadPool: ExecutorService): List<FileLineOwnedByUser> {
        log.info("Start processing latest commit")

        val filePaths = GitMinerUtil.getAllFilePathsOnCommit(repository, latestCommit)

        val concurrentLinkedQueue = ConcurrentLinkedQueue<FileLineOwnedByUser>()

        val tasks = mutableListOf<Runnable>()
        for (filePath in filePaths) {
            if (isNotNeededFilePath(filePath, filesToProceed)) continue

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

        HelpFunctionsUtil.runInThreadPoolWithExceptionHandle(threadPool, tasks)

        log.info("End processing latest commit")

        return concurrentLinkedQueue.toList()
    }
}
