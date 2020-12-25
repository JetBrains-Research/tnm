package gitMiners

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import util.FileMapper
import util.UserMapper

object UtilGitMiner {
    // TODO: mb change git, reader logic
    /**
     * Get diffs between [commit1] and [commit2].
     *
     * @param commit1 RevCommit
     * @param commit2 RevCommit
     * @param reader must be created from the same Repository as [git]
     * @param git must be created from the same Repository as [reader]
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
     * @param reader must be created from the same Repository as git
     * @param git must be created from the same Repository as [reader]
     * @return set of changed files ids
     */
    fun getChangedFiles(commit1: RevCommit, commit2: RevCommit, reader: ObjectReader, git: Git): Set<Int> {
        val result = mutableSetOf<Int>()
        val diffs = UtilGitMiner.getDiffs(commit1, commit2, reader, git)
        val userEmail = commit1.authorIdent.emailAddress
        UserMapper.add(userEmail)

        for (entry in diffs) {
            val fileId = FileMapper.add(entry.oldPath)
            result.add(fileId)
        }
        return result
    }

}