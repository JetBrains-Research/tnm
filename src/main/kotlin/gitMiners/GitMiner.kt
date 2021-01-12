package gitMiners

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.revwalk.RevCommit
import util.CommitMapper
import util.ProjectConfig
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

// TODO: add better logic for count commits
abstract class GitMiner(
    protected val repository: FileRepository, val neededBranches: Set<String>,
    protected val reversed: Boolean = false,
    protected val numThreads: Int = ProjectConfig.numThreads
) {
    protected val git = Git(repository)
    protected val reader: ObjectReader = repository.newObjectReader()
    private val comparedCommits = HashMap<Int, MutableSet<Int>>()

    /**
     * Mine all needed data from pair of commits.
     * [prevCommit] is always older than [currCommit].
     *
     * @param currCommit RevCommit which must be earlier then [prevCommit]
     * @param prevCommit RevCommit which must be older then [currCommit]
     */
    protected abstract fun process(currCommit: RevCommit, prevCommit: RevCommit)

    /**
     * Mine all needed data from [repository]. In default realisation iterates through
     * pairs of commits in DESC order while applying [process] function.
     *
     */
    open fun run() {
        val branches = UtilGitMiner.findNeededBranchesOrNull(git, neededBranches) ?: return

        for (branch in branches) {
            println("Start mining for branch ${UtilGitMiner.getShortBranchName(branch.name)}")

            val commitsCount = countCommits(branch.name)
            var currentCommitIndex = 0
            val logFrequency = 100
            val commitsInBranch = UtilGitMiner.getCommits(git, repository, branch.name, reversed)
            for ((currCommit, prevCommit) in commitsInBranch.windowed(2)) {
                if (!addProceedCommits(currCommit, prevCommit)) continue
                if (++currentCommitIndex % logFrequency == 0) {
                    println("Processed $currentCommitIndex commits of $commitsCount")
                }
                process(currCommit, prevCommit)
            }

            // TODO: last commit and empty tree
//            val empty = repository.resolve("")
            println("End mining for branch ${UtilGitMiner.getShortBranchName(branch.name)}")
        }
    }

    fun multithreadingRun() {
        val branches = UtilGitMiner.findNeededBranchesOrNull(git, neededBranches) ?: return
        val threadPool = Executors.newFixedThreadPool(numThreads)

        for (branch in branches) {
            println("Start mining for branch ${UtilGitMiner.getShortBranchName(branch.name)}")

            val commitsCount = countCommits(branch.name)
            val proceedCommits = AtomicInteger(0)
            val logFrequency = 100

            val latch = CountDownLatch(commitsCount)

            val commitsInBranch = UtilGitMiner.getCommits(git, repository, branch.name, reversed)
            for ((currCommit, prevCommit) in commitsInBranch.windowed(2)) {
                if (!addProceedCommits(currCommit, prevCommit)) continue

                threadPool.execute {
                    try {
                        process(currCommit, prevCommit)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        val num = proceedCommits.incrementAndGet()
                        if (num % logFrequency == 0 || num == commitsCount) {
                            println("Processed $num commits of $commitsCount")
                        }

                        latch.countDown()
                    }
                }
            }
            latch.await()
            println("End mining for branch ${UtilGitMiner.getShortBranchName(branch.name)}")
        }
        threadPool.shutdown()
    }

    /**
     * Saves to json all mined data.
     *
     */
    abstract fun saveToJson(resourceDirectory: File)

    protected fun addProceedCommits(currCommit: RevCommit, prevCommit: RevCommit): Boolean {
        val currCommitId = CommitMapper.add(currCommit.name)
        val prevCommitId = CommitMapper.add(prevCommit.name)
        // TODO: better logic?
        val addForCurr = comparedCommits.computeIfAbsent(currCommitId) { mutableSetOf() }.add(prevCommitId)
        val addForPrev = comparedCommits.computeIfAbsent(prevCommitId) { mutableSetOf() }.add(currCommitId)
        return addForCurr || addForPrev
    }

    protected fun checkProceedCommits(currCommit: RevCommit, prevCommit: RevCommit): Boolean {
        val currCommitId = CommitMapper.add(currCommit.name)
        val prevCommitId = CommitMapper.add(prevCommit.name)
        // TODO: better logic?
        return comparedCommits.computeIfAbsent(currCommitId) { mutableSetOf() }.contains(prevCommitId) ||
                comparedCommits.computeIfAbsent(prevCommitId) { mutableSetOf() }.contains(currCommitId)
    }

    protected fun countCommits(branchName: String): Int {
        var commitsCount = 0
        val commitsInBranch = UtilGitMiner.getCommits(git, repository, branchName, reversed)
        for ((currCommit, prevCommit) in commitsInBranch.windowed(2)) {
            if (checkProceedCommits(currCommit, prevCommit)) continue
            commitsCount++
        }
        return commitsCount
    }
}
