package miners.gitMiners

import dataProcessor.DataProcessor
import miners.Miner
import miners.gitMiners.exceptions.NotGitRepositoryException
import miners.gitMiners.exceptions.ProcessInThreadPoolException
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.util.io.DisabledOutputStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import util.HelpFunctionsUtil
import util.ProjectConfig
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger


abstract class GitMiner<T>(
    repositoryFile: File, val neededBranches: Set<String>,
    val numOfCommits: Int? = null,
    protected val numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) : Miner<T> where T : DataProcessor<*> {
    
    private val log: Logger = LoggerFactory.getLogger(GitMiner::class.java)

    companion object {
        const val EMPTY_COMMIT_SHA = "4b825dc642cb6eb9a060e54bf8d69288fbee4904"
    }

    init {
        if (!HelpFunctionsUtil.isGitRepository(repositoryFile)) {
            throw NotGitRepositoryException("${repositoryFile.absolutePath} is not git repository")
        }
    }

    protected val repository = FileRepository(repositoryFile)
    protected val comparedCommits = HashMap<String, MutableSet<String>>()
    protected val logFrequency = 100

    protected val threadLocalGit = object : ThreadLocal<Git>() {
        override fun initialValue(): Git {
            return Git(repository)
        }
    }

    protected val threadLocalReader = object : ThreadLocal<ObjectReader>() {
        override fun initialValue(): ObjectReader {
            return repository.newObjectReader()
        }
    }

    protected val threadLocalDiffFormatter = object : ThreadLocal<DiffFormatter>() {
        override fun initialValue(): DiffFormatter {
            val diffFormatter = DiffFormatter(DisabledOutputStream.INSTANCE)
            diffFormatter.setRepository(repository)
            diffFormatter.setDiffComparator(RawTextComparator.DEFAULT)
            diffFormatter.isDetectRenames = true
            return diffFormatter
        }
    }

    /**
     * Mine all needed data from commit.
     *
     * @param commit RevCommit
     */
    protected abstract fun process(dataProcessor: T, commit: RevCommit)

    /**
     * Mine all needed data from [repository]. In default realisation iterates through
     * pairs of commits in DESC order while applying [process] function.
     *
     */
    override fun run(dataProcessor: T) {
        val branches = GitMinerUtil.findNeededBranches(threadLocalGit.get(), neededBranches)
        val threadPool = Executors.newFixedThreadPool(numThreads)
        processAllCommitsInThreadPool(branches, dataProcessor, threadPool)
        threadPool.shutdown()
        dataProcessor.calculate()
    }

    private fun processAllCommitsInThreadPool(branches: Set<Ref>, dataProcessor: T, threadPool: ExecutorService) {
        for (branch in branches) {
            log.info("Start mining for branch ${GitMinerUtil.getShortBranchName(branch.name)}")

            val commitsInBranch = getUnprocessedCommits(branch.name)
            if (commitsInBranch.isEmpty()) {
                log.info("Nothing to proceed in branch $branch")
                continue
            }

            val proceedCommits = AtomicInteger(0)
            val futures = mutableListOf<Future<*>>()

            for (commit in commitsInBranch) {
                if (commit.parents.size > 1) continue
                // TODO: need change
                if (!addProceedCommits(commit)) continue

                val runnable = Runnable {
                    try {
                        process(dataProcessor, commit)
                    } catch (e: Exception) {
                        val msg = "Got error while processing commit ${commit.name}"
                        throw ProcessInThreadPoolException(msg, e)
                    } finally {
                        val num = proceedCommits.incrementAndGet()
                        if (num % logFrequency == 0 || num == commitsInBranch.size) {
                            log.info("Processed $num commits out of ${commitsInBranch.size}")
                        }

                    }
                }

                futures.add(threadPool.submit(runnable))

            }

            for (future in futures) {
                try {
                    future.get()
                } catch (e: Exception) {
                    threadPool.shutdownNow()
                    throw e
                }
            }


            log.info("End mining for branch ${GitMinerUtil.getShortBranchName(branch.name)}")
        }
    }

    // TODO: replace, with ids? move to data processor?
    protected fun addProceedCommits(commit: RevCommit): Boolean {
        val prevCommitSHA = if (commit.parents.isNotEmpty()) commit.parents[0].name else EMPTY_COMMIT_SHA

        val addForCurr = comparedCommits.computeIfAbsent(commit.name) { mutableSetOf() }.add(prevCommitSHA)
        val addForPrev = comparedCommits.computeIfAbsent(prevCommitSHA) { mutableSetOf() }.add(commit.name)
        return addForCurr || addForPrev
    }

    protected fun checkProceedCommits(commit: RevCommit): Boolean {
        val prevCommitSHA = if (commit.parents.isNotEmpty()) commit.parents[0].name else EMPTY_COMMIT_SHA

        return comparedCommits.computeIfAbsent(commit.name) { mutableSetOf() }.contains(prevCommitSHA) ||
            comparedCommits.computeIfAbsent(prevCommitSHA) { mutableSetOf() }.contains(commit.name)
    }

    protected fun getUnprocessedCommits(branchName: String): List<RevCommit> {
        val result = linkedSetOf<RevCommit>()
        val commitsInBranch = GitMinerUtil.getCommits(threadLocalGit.get(), repository, branchName, numOfCommits)
        for (commit in commitsInBranch) {
            if (checkProceedCommits(commit)) continue
            result.add(commit)
        }
        return result.toList()
    }

    fun clearComparedCommitsHistory() {
        comparedCommits.clear()
    }

}
