package miners.gitMiners

import dataProcessor.inputData.entity.FileEdit
import miners.gitMiners.exceptions.BranchNotExistsException
import org.eclipse.jgit.api.BlameCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.Edit
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.EmptyTreeIterator
import org.eclipse.jgit.treewalk.TreeWalk
import util.mappers.FileMapper
import util.mappers.UserMapper
import java.io.ByteArrayOutputStream

object GitMinerUtil {
    private const val ADD_MARK = '+'
    private const val DELETE_MARK = '-'
    private const val DIFF_MARK = '@'
    private val changeLinesRegex = Regex("@@ -(\\d+)(,\\d+)? \\+(\\d+)(,\\d+)? @@")
    private val bugFixRegex = Regex("\\b[Ff]ix:?\\b")

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
        return isBugFixCommit(commit.shortMessage, commit.fullMessage)
    }

    fun isBugFixCommit(shortMessage: String, fullMessage: String): Boolean {
        val shortMsgContains = bugFixRegex.find(shortMessage) != null
        val fullMsgContains = bugFixRegex.find(fullMessage) != null
        return shortMsgContains || fullMsgContains
    }

    fun getCommits(
        git: Git,
        repository: Repository,
        branchName: String,
        maxCount: Int? = null
    ): List<RevCommit> {
        val command = git.log().add(repository.resolve(branchName))
        maxCount?.let { command.setMaxCount(it) }
        return command.call().toList()
    }


    fun getAllFilePathsOnCommit(repository: Repository, commit: RevCommit): List<String> {
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

    fun getDiffFormatterWithBuffer(
        repository: Repository,
        out: ByteArrayOutputStream
    ): DiffFormatter {
        val diffFormatter = DiffFormatter(out)
        diffFormatter.setRepository(repository)
        diffFormatter.setDiffComparator(RawTextComparator.DEFAULT)
        diffFormatter.isDetectRenames = true
        diffFormatter.setContext(0)
        return diffFormatter
    }

    fun getFileEdits(
        commit: RevCommit,
        repository: Repository,
        reader: ObjectReader,
        git: Git,
    ): List<FileEdit> {
        val out = ByteArrayOutputStream()
        val diffFormatter = getDiffFormatterWithBuffer(repository, out)
        return getFileEdits(commit, reader, git, out, diffFormatter)
    }

    fun getFileEdits(
        commit: RevCommit,
        reader: ObjectReader,
        git: Git,
        out: ByteArrayOutputStream,
        diffFormatter: DiffFormatter
    ): List<FileEdit> {
        // get all diffs and then proceed separately
        val diffs = reader.use {
            getDiffsWithoutText(commit, it, git)
        }

        val edits = mutableListOf<FileEdit>()

        for (diff in diffs) {
            val deleteBlock = mutableListOf<String>()
            val addBlock = mutableListOf<String>()

            var start = false
            diffFormatter.format(diff)
            val diffText = out.toString("UTF-8").split("\n")
            var preStartLineNum = 0
            var postStartLineNum = 0

            val oldPath = getFilePath(diff.oldPath)
            val newPath = getFilePath(diff.newPath)

            for (line in diffText) {
                if (line.isEmpty()) continue

                val mark = line[0]

                // pass until diffs
                if (!start) {
                    if (mark == DIFF_MARK) {
                        start = true
                    } else {
                        continue
                    }
                }

                when (mark) {
                    ADD_MARK -> {
                        addBlock.add(line.substring(1))
                    }
                    DELETE_MARK -> {
                        deleteBlock.add(line.substring(1))
                    }
                    DIFF_MARK -> {
                        if (addBlock.isNotEmpty() || deleteBlock.isNotEmpty()) {
                            val data = FileEdit(
                                addBlock.toList(), deleteBlock.toList(), preStartLineNum,
                                postStartLineNum, oldPath, newPath
                            )
                            edits.add(data)

                            addBlock.clear()
                            deleteBlock.clear()
                        }

                        val match = changeLinesRegex.find(line)!!
                        preStartLineNum = match.groupValues[1].toInt()
                        postStartLineNum = match.groupValues[3].toInt()
                    }
                }

            }

            val data = FileEdit(
                addBlock, deleteBlock, preStartLineNum,
                postStartLineNum, oldPath, newPath
            )
            edits.add(data)


            out.reset()
        }

        return edits
    }

    private fun getFilePath(path: String): String {
        if (path == DiffEntry.DEV_NULL) return ""
        // delete prefixes a/, b/ of DiffFormatter
        return path.substring(2)
    }

    private fun getCommitsForLines(repository: Repository, commit: RevCommit, fileName: String): List<String> {
        val result = ArrayList<String>()

        val blamer = BlameCommand(repository)
        blamer.setStartCommit(commit.id)
        blamer.setFilePath(fileName)
        val blame = blamer.call()

        val resultContents = blame.resultContents

        for (i in 0 until resultContents.size()) {
            val commitOfLine = blame.getSourceCommit(i)
            result.add(commitOfLine.name)
        }

        return result
    }


    fun getCommitsAdj(diffs: List<DiffEntry>, prevCommit: RevCommit, repository: Repository, diffFormatter: DiffFormatter): Set<String> {
        val commitsAdj = mutableSetOf<String>()
        val filesCommits = mutableMapOf<String, List<String>>()
        for (diff in diffs) {
            if (diff.changeType != DiffEntry.ChangeType.MODIFY) continue
            val fileName = getFilePath(diff)

            var prevCommitBlame = listOf<String>()

            if (!filesCommits.containsKey(fileName)) {
                prevCommitBlame = getCommitsForLines(repository, prevCommit, fileName)
                filesCommits[fileName] = prevCommitBlame
            } else {
                val list = filesCommits[fileName]
                if (list != null) {
                    prevCommitBlame = list
                }
            }

            val editList = diffFormatter.toFileHeader(diff).toEditList()
            for (edit in editList) {
                if (edit.type != Edit.Type.REPLACE && edit.type != Edit.Type.DELETE) continue
                val lines = edit.beginA until edit.endA

                for (line in lines) {
                    commitsAdj.add(prevCommitBlame[line])
                }
            }

        }
        return commitsAdj
    }

}
