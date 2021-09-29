package miners.gitMiners

import miners.gitMiners.exceptions.BranchNotExistsException
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.EmptyTreeIterator
import org.eclipse.jgit.treewalk.TreeWalk
import util.mappers.FileMapper
import util.mappers.UserMapper

object UtilGitMiner {
    /**
     * Get diffs for [commit].
     *
     * @param commit RevCommit
     * @param reader must be created from the same Repository as [git]
     * @param git must be created from the same Repository as [reader]
     * @return List of DiffEntry's for [commit].
     */
    fun getDiffsWithoutText(
        commit: RevCommit,
        reader: ObjectReader,
        git: Git
    ): List<DiffEntry> {
        val oldTreeIter = if (commit.parents.isNotEmpty()) {
            val firstParent = commit.parents[0]
            val treeParser = CanonicalTreeParser()
            treeParser.reset(reader, firstParent.tree)
            treeParser
        } else EmptyTreeIterator()

        val newTreeIter = CanonicalTreeParser()
        newTreeIter.reset(reader, commit.tree)

        return git.diff()
            .setNewTree(newTreeIter)
            .setOldTree(oldTreeIter)
            .setShowNameAndStatusOnly(true)
            .call()
    }

    /**
     * Get changed files between [commit].
     *
     * @param commit RevCommit
     * @param reader must be created from the same Repository as git
     * @param git must be created from the same Repository as [reader]
     * @return set of changed files ids
     */
    fun getChangedFiles(
        commit: RevCommit,
        reader: ObjectReader,
        git: Git,
        userMapper: UserMapper,
        fileMapper: FileMapper
    ): Set<Int> {
        val result = mutableSetOf<Int>()
        val diffs = getDiffsWithoutText(commit, reader, git)
        val userEmail = commit.authorIdent.emailAddress
        userMapper.add(userEmail)

        for (entry in diffs) {
            val fileId = fileMapper.add(getFilePath(entry))
            result.add(fileId)
        }
        return result
    }

    fun getFilePath(diffEntry: DiffEntry): String {
        return if (diffEntry.changeType == DiffEntry.ChangeType.DELETE) {
            diffEntry.oldPath
        } else {
            diffEntry.newPath
        }
    }

    fun getChangedFiles(
        commit: RevCommit,
        reader: ObjectReader,
        git: Git
    ): Set<String> {
        val result = mutableSetOf<String>()
        val diffs = getDiffsWithoutText(commit, reader, git)

        for (entry in diffs) {
            result.add(getFilePath(entry))
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
    fun findNeededBranches(git: Git, neededBranches: Set<String>): Set<Ref> {
        val result = mutableSetOf<Ref>()
        val neededBranchesMutable = neededBranches.toMutableSet()
        val allBranches = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call()
        for (branch in allBranches) {
            val shortBranchName = getShortBranchName(branch.name)
            if (shortBranchName in neededBranchesMutable) {
                neededBranchesMutable.remove(shortBranchName)
                result.add(branch)
            }
        }

        if (neededBranchesMutable.isNotEmpty()) {
            val allShortBranches = allBranches.map { getShortBranchName(it.name) }
            throw BranchNotExistsException(neededBranchesMutable.toList(), allShortBranches)
        }

        return result
    }

    fun findNeededBranch(git: Git, neededBranch: String): Ref {
        val allBranches = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call()
        for (branch in allBranches) {
            val shortBranchName = getShortBranchName(branch.name)
            if (shortBranchName == neededBranch) {
                return branch
            }
        }

        val allShortBranches = allBranches.map { getShortBranchName(it.name) }
        throw BranchNotExistsException(listOf(neededBranch), allShortBranches)
    }


    fun isBugFixCommit(commit: RevCommit): Boolean {
        val regex = "\\b[Ff]ix:?\\b".toRegex()
        val shortMsgContains = regex.find(commit.shortMessage) != null
        val fullMsgContains = regex.find(commit.fullMessage) != null
        return shortMsgContains || fullMsgContains
    }

    fun getCommits(
        git: Git,
        repository: FileRepository,
        branchName: String,
        maxCount: Int? = null
    ): List<RevCommit> {
        val command = git.log().add(repository.resolve(branchName))
        maxCount?.let { command.setMaxCount(it) }
        return command.call().toList()
    }


    fun getAllFilePathsOnCommit(repository: FileRepository, commit: RevCommit): List<String> {
        val filePaths = mutableListOf<String>()

        val treeWalk = TreeWalk(repository)
        treeWalk.addTree(commit.tree)
        treeWalk.isRecursive = false

        while (treeWalk.next()) {
            if (treeWalk.isSubtree) {
                treeWalk.enterSubtree()
                continue
            }
            val filePath = treeWalk.pathString
            filePaths.add(filePath)
        }

        return filePaths
    }

    fun isNotNeededFilePath(filePath: String, filesToProceed: Set<String>?): Boolean {
        if (filesToProceed != null) {
            return filePath !in filesToProceed
        }
        return false
    }

}
