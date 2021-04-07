package gitMiners

import dataProcessor.DataProcessor
import gitMiners.exceptions.ProcessInThreadPoolException
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.util.io.DisabledOutputStream
import util.ProjectConfig
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger

abstract class GitMiner<T>(
    protected val repository: FileRepository, val neededBranches: Set<String>,
    protected val reversed: Boolean = false,
    protected val numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) where T : DataProcessor<*> {
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

    protected val comparedCommits = HashMap<String, MutableSet<String>>()
    protected val logFrequency = 100

    /**
     * Mine all needed data from pair of commits.
     * [prevCommit] is always older than [currCommit].
     *
     * @param currCommit RevCommit which must be earlier then [prevCommit]
     * @param prevCommit RevCommit which must be older then [currCommit]
     */
    protected abstract fun process(dataProcessor: T, currCommit: RevCommit, prevCommit: RevCommit)

    /**
     * Mine all needed data from [repository]. In default realisation iterates through
     * pairs of commits in DESC order while applying [process] function.
     *
     */
    open fun run(dataProcessor: T) {
        val branches = UtilGitMiner.findNeededBranches(threadLocalGit.get(), neededBranches)
        val threadPool = Executors.newFixedThreadPool(numThreads)
        processAllCommitsInThreadPool(branches, dataProcessor, threadPool)
        threadPool.shutdown()
    }

    private fun processAllCommitsInThreadPool(branches: Set<Ref>, dataProcessor: T, threadPool: ExecutorService) {
        for (branch in branches) {
            println("Start mining for branch ${UtilGitMiner.getShortBranchName(branch.name)}")

            val commitsInBranch = getUnprocessedCommits(branch.name)
            val commitsPairsCount = commitsInBranch.size - 1
            if (commitsPairsCount == 0 || commitsPairsCount == -1) {
                println("Nothing to proceed in branch $branch")
                continue
            }

            val proceedCommits = AtomicInteger(0)
            val futures = mutableListOf<Future<*>>()

            for ((currCommit, prevCommit) in commitsInBranch.windowed(2)) {
                if (!addProceedCommits(currCommit, prevCommit)) continue

                val runnable = Runnable {
                    try {
                        process(dataProcessor, currCommit, prevCommit)
                    } catch (e: Exception) {
                        val msg = "Got error while processing commits ${currCommit.name} and ${prevCommit.name}"
                        throw ProcessInThreadPoolException(msg, e)
                    } finally {
                        val num = proceedCommits.incrementAndGet()
                        if (num % logFrequency == 0 || num == commitsPairsCount) {
                            println("Processed $num commits of $commitsPairsCount")
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


            println("End mining for branch ${UtilGitMiner.getShortBranchName(branch.name)}")
        }
    }

    // TODO: replace, with ids? move to data processor?
    protected fun addProceedCommits(currCommit: RevCommit, prevCommit: RevCommit): Boolean {
        val addForCurr = comparedCommits.computeIfAbsent(currCommit.name) { mutableSetOf() }.add(prevCommit.name)
        val addForPrev = comparedCommits.computeIfAbsent(prevCommit.name) { mutableSetOf() }.add(currCommit.name)
        return addForCurr || addForPrev
    }

    protected fun checkProceedCommits(currCommit: RevCommit, prevCommit: RevCommit): Boolean {
        return comparedCommits.computeIfAbsent(currCommit.name) { mutableSetOf() }.contains(prevCommit.name) ||
                comparedCommits.computeIfAbsent(prevCommit.name) { mutableSetOf() }.contains(currCommit.name)
    }

    protected fun getUnprocessedCommits(branchName: String): List<RevCommit> {
        val result = linkedSetOf<RevCommit>()
        val commitsInBranch = UtilGitMiner.getCommits(threadLocalGit.get(), repository, branchName, reversed)
        for ((currCommit, prevCommit) in commitsInBranch.windowed(2)) {
            if (checkProceedCommits(currCommit, prevCommit)) continue
            result.add(currCommit)
            result.add(prevCommit)
        }
        return result.toList()
    }

}
