package gitMiners

import com.google.gson.Gson
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import util.FileMapper
import util.UserMapper

abstract class GitMiner {

    abstract val repository: Repository
    abstract val git: Git
    abstract val reader: ObjectReader
    abstract val gson: Gson

    companion object {
        // TODO: mb change git, reader logic

        /**
         * Get diffs between [commit1] and [commit2].
         *
         * @param commit1 RevCommit
         * @param commit2 RevCommit
         * @param reader must be created from same Repository as [git]
         * @param git must be created from same Repository as [reader]
         * @return List of DiffEntry's between [commit1] and [commit2].
         */
        fun getDiffs(commit1: RevCommit, commit2: RevCommit, reader: ObjectReader, git: Git): List<DiffEntry> {
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
         * Get changed files between [commit1] and [commit2].
         *
         * @param commit1 RevCommit
         * @param commit2 RevCommit
         * @param reader must be created from same Repository as git
         * @param git must be created from same Repository as [reader]
         * @return set of changed files ids
         */
        fun getChangedFiles(commit1: RevCommit, commit2: RevCommit, reader: ObjectReader, git: Git): Set<Int> {
            val result = mutableSetOf<Int>()
            val diffs = getDiffs(commit1, commit2, reader, git)
            val userEmail = commit1.authorIdent.emailAddress
            UserMapper.add(userEmail)

            for (entry in diffs) {
                val fileId = FileMapper.add(entry.oldPath)
                result.add(fileId)
            }
            return result
        }
    }

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
    abstract fun saveToJson()
}