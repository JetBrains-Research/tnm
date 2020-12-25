package gitMiners

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import util.FileMapper
import util.ProjectConfig
import util.UserMapper
import java.io.File

abstract class GitMiner {

    abstract val repository: Repository
    abstract val git: Git
    abstract val reader: ObjectReader

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
        val branches: List<Ref> = git.branchList().call()

        for (branch in branches) {
            val commitsInBranch = git.log().add(repository.resolve(branch.name)).call()

            for ((currCommit, prevCommit) in commitsInBranch.windowed(2)) {
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