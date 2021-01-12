package gitMiners

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.lib.Ref
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
        val diffs = getDiffs(commit1, commit2, reader, git)
        val userEmail = commit1.authorIdent.emailAddress
        UserMapper.add(userEmail)

        for (entry in diffs) {
            val fileId = FileMapper.add(entry.oldPath)
            result.add(fileId)
        }
        return result
    }

    /**
     * Get short branch name. Removes first two parts before '/'.
     * For example this parts could be 'refs/remotes/' or 'refs/head/'
     *
     * @param branchName full branch name from branch.name call
     * @return short branch name
     */
    fun getShortBranchName(branchName: String): String {
        val index = branchName.indexOf("/", branchName.indexOf("/") + 1)
        if (index == -1) return branchName
        return branchName.substring(index + 1, branchName.length)
    }

    fun getBranchesShortNames(git: Git): Set<String> {
        return git
            .branchList()
            .setListMode(ListBranchCommand.ListMode.ALL)
            .call()
            .map { getShortBranchName(it.name) }
            .toSet()
    }

    /**
     * Look for [neededBranches] in [git]. Proceed each branch name with [getShortBranchName]
     * and check if [neededBranches] contains it. If it's true store in result.
     * If all branches are found returns result, otherwise null.
     *
     * @param git git where to look for branches
     * @param neededBranches set of needed branches
     * @return set of Refs for needed branches or null
     */
    fun findNeededBranchesOrNull(git: Git, neededBranches: Set<String>): Set<Ref>? {
        val result = mutableSetOf<Ref>()
        val needed = neededBranches.toMutableSet()
        val allBranches = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call()
        for (branch in allBranches) {
            val shortBranchName = getShortBranchName(branch.name)
            if (shortBranchName in needed) {
                needed.remove(shortBranchName)
                result.add(branch)
            }
        }

        if (needed.isNotEmpty()) {
            println("Couldn't find branches:")
            needed.forEach { println(it) }
            println("Known branches:")
            allBranches.forEach { println(getShortBranchName(it.name)) }
            return null
        }
        return result
    }

    fun getCommits(
        git: Git,
        repository: FileRepository,
        branchName: String,
        reversed: Boolean = false
    ): List<RevCommit> {
        return if (reversed) {
            git.log().add(repository.resolve(branchName)).call().reversed()
        } else {
            git.log().add(repository.resolve(branchName)).call().toList()
        }
    }
}
