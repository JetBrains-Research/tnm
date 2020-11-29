package gitMiners

import com.google.gson.Gson
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.CanonicalTreeParser

interface GitMiner {

    val repository: Repository
    val git: Git
    val reader: ObjectReader
    val gson: Gson

    /**
     * Mine all needed data from pair of commits.
     * [prevCommit] is always older than [currCommit].
     * TODO: All fields with mined results must call needed calculations in getters
     *
     * @param currCommit RevCommit which must be earlier then [prevCommit]
     * @param prevCommit RevCommit which must be older then [currCommit]
     */
    fun process(currCommit: RevCommit, prevCommit: RevCommit)

    /**
     * Mine all needed data from [repository]. In default realisation iterates through
     * pairs of commits in DESC order while applying [process] function.
     *
     */
    fun run() {
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
     * Get diffs between [commit1] and [commit2].
     *
     * @param commit1 RevCommit
     * @param commit2 RevCommit
     * @return List of DiffEntry's between [commit1] and [commit2].
     */
    fun getDiffs(commit1: RevCommit, commit2: RevCommit): List<DiffEntry> {
        val oldTreeIter = CanonicalTreeParser()
        oldTreeIter.reset(reader, commit2.tree)

        val newTreeIter = CanonicalTreeParser()
        newTreeIter.reset(reader, commit1.tree)

        return git.diff()
                .setNewTree(newTreeIter)
                .setOldTree(oldTreeIter)
                .call()
    }

    /**
     * Saves to json all mined data.
     *
     */
    fun saveToJson()
}