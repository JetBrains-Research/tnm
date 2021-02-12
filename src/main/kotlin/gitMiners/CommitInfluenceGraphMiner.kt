package gitMiners

import org.eclipse.jgit.api.BlameCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.Edit
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.util.io.DisabledOutputStream
import util.*
import java.io.File


/**
 * Page rank miner
 *
 * @property repository working repository
 * @constructor Create Page rank miner for [repository] and store the results
 */
class CommitInfluenceGraphMiner(
    repository: FileRepository,
    neededBranches: Set<String> = ProjectConfig.neededBranches,
    numThreads: Int = ProjectConfig.numThreads
) : GitMiner(repository, neededBranches, numThreads = numThreads, reversed = true) {

    // H is the transition probability matrix whose (i, j)
    // element signifies the probability of transition from the i-th page to the j-th page
    // pages - commits
    // TODO: probability == 1 ?
    private val commitsGraph = Graph<Int>()

    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        val git = Git(repository)
        val reader = repository.newObjectReader()

        val currCommitId = CommitMapper.add(currCommit.name)
        val prevCommitId = CommitMapper.add(prevCommit.name)

        if (isBugFix(currCommit)) {
            commitsGraph.addNode(currCommitId)
            commitsGraph.addNode(prevCommitId)

            val diffs = UtilGitMiner.getDiffsWithoutText(currCommit, prevCommit, reader, git)

            val commitsAdj = getCommitsAdj(diffs, prevCommit)
            for (commitId in commitsAdj) {
                commitsGraph.addEdge(currCommitId, commitId)
            }
        }
    }

    private fun getCommitsForLines(commit: RevCommit, fileName: String): List<String> {
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

    private fun getCommitsAdj(diffs: List<DiffEntry>, prevCommit: RevCommit): Set<Int> {
        val commitsAdj = mutableSetOf<Int>()
        val filesCommits = mutableMapOf<Int, List<String>>()
        for (diff in diffs) {
            if (diff.changeType != DiffEntry.ChangeType.MODIFY) continue
            val fileName = diff.oldPath
            val fileId = FileMapper.add(fileName)

            var prevCommitBlame = listOf<String>()

            if (!filesCommits.containsKey(fileId)) {
                prevCommitBlame = getCommitsForLines(prevCommit, fileName)
                filesCommits[fileId] = prevCommitBlame
            } else {
                val list = filesCommits[fileId]
                if (list != null) {
                    prevCommitBlame = list
                }
            }

            val diffFormatter = DiffFormatter(DisabledOutputStream.INSTANCE)
            diffFormatter.setRepository(repository)
            diffFormatter.setDiffComparator(RawTextComparator.DEFAULT)
            diffFormatter.isDetectRenames = true

            val editList = diffFormatter.toFileHeader(diff).toEditList()
            for (edit in editList) {
                if (edit.type != Edit.Type.REPLACE && edit.type != Edit.Type.DELETE) continue
                val lines = edit.beginA until edit.endA

                for (line in lines) {
                    val commitId = CommitMapper.add(prevCommitBlame[line])
                    commitsAdj.add(commitId)
                }
            }

        }
        return commitsAdj
    }

    private fun isBugFix(commit: RevCommit): Boolean {
        val regex = "\\bfix:?\\b".toRegex()
        val shortMsgContains = regex.find(commit.shortMessage) != null
        val fullMsgContains = regex.find(commit.fullMessage) != null
        return shortMsgContains || fullMsgContains
    }

    override fun saveToJson(resourceDirectory: File) {
        val map = hashMapOf<Int, MutableSet<Int>>()
        for (entry in commitsGraph.adjacencyMap.entries) {
            map[entry.key] = entry.value
        }

        UtilFunctions.saveToJson(File(resourceDirectory, ProjectConfig.COMMITS_GRAPH), map)
        Mapper.saveAll(resourceDirectory)
    }

}
