package gitMiners

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import java.io.File

abstract class GitMiner(protected val repository: Repository, val neededBranches: Set<String>) {
    protected val git = Git(repository)
    protected val reader: ObjectReader = repository.newObjectReader()

    /**
     * Mine all needed data from pair of commits.
     * [prevCommit] is always older than [currCommit].
     * TODO: All fields with mined results must call needed calculations in getters
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
            var commitsCount = 0
            for ((_, _) in git.log().add(repository.resolve(branch.name)).call().windowed(2)) {
                commitsCount++
            }

            var currentCommitIndex = 0
            val logFrequency = 100
            val commitsInBranch = git.log().add(repository.resolve(branch.name)).call()
            for ((currCommit, prevCommit) in commitsInBranch.windowed(2)) {
                if (++currentCommitIndex % logFrequency == 0) {
                    println("Processed $currentCommitIndex commits of $commitsCount")
                }
                process(currCommit, prevCommit)
            }

            // TODO: last commit and empty tree
//            val empty = repository.resolve("")
        }
    }

    /**
     * Saves to json all mined data.
     *
     */
    abstract fun saveToJson(resourceDirectory: File)
}