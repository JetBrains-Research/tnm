package miners.gitMiners

import dataProcessor.CommitInfluenceGraphDataProcessor
import dataProcessor.inputData.CommitInfluenceInfo
import miners.gitMiners.UtilGitMiner.isBugFixCommit
import org.eclipse.jgit.api.BlameCommand
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.Edit
import org.eclipse.jgit.revwalk.RevCommit
import util.ProjectConfig
import java.io.File


/**
 * Page rank miner
 *
 * @property repository working repository
 * @constructor Create Page rank miner for [repository] and store the results
 */
class CommitInfluenceGraphMiner(
    repositoryFile: File,
    neededBranches: Set<String>,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) : GitMiner<CommitInfluenceGraphDataProcessor>(
    repositoryFile,
    neededBranches,
    numThreads = numThreads
) {

    override fun process(
        dataProcessor: CommitInfluenceGraphDataProcessor,
        commit: RevCommit
    ) {
        if (isBugFixCommit(commit)) {
            val git = threadLocalGit.get()
            val reader = threadLocalReader.get()
            // TODO: strange case
            val prevCommit = if (commit.parents.isNotEmpty()) commit.parents[0] else return
            val diffs =
                reader.use { UtilGitMiner.getDiffsWithoutText(commit, it, git) }

            val adjCommits = getCommitsAdj(diffs, prevCommit)
            val data = CommitInfluenceInfo(commit.name, prevCommit.name, adjCommits)
            dataProcessor.processData(data)
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

    private fun getCommitsAdj(diffs: List<DiffEntry>, prevCommit: RevCommit): Set<String> {
        val commitsAdj = mutableSetOf<String>()
        val filesCommits = mutableMapOf<String, List<String>>()
        for (diff in diffs) {
            if (diff.changeType != DiffEntry.ChangeType.MODIFY) continue
            val fileName = diff.oldPath

            var prevCommitBlame = listOf<String>()

            if (!filesCommits.containsKey(fileName)) {
                prevCommitBlame = getCommitsForLines(prevCommit, fileName)
                filesCommits[fileName] = prevCommitBlame
            } else {
                val list = filesCommits[fileName]
                if (list != null) {
                    prevCommitBlame = list
                }
            }

            val diffFormatter = threadLocalDiffFormatter.get()

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
