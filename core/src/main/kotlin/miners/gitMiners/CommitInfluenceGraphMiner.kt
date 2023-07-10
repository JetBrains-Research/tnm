package miners.gitMiners

import dataProcessor.CommitInfluenceGraphDataProcessor
import dataProcessor.inputData.CommitInfluenceInfo
import miners.gitMiners.GitMinerUtil.isBugFixCommit
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
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
            val reader = threadLocalReader.get()
            // TODO: strange case
            val prevCommit = if (commit.parents.isNotEmpty()) commit.parents[0] else return
            val diffs = reader.use { GitMinerUtil.getDiffsWithoutText(commit, it, repository) }
            val adjCommits =
                GitMinerUtil.getCommitsAdj(diffs, prevCommit, repository, threadLocalDiffFormatter.get())
            val data = CommitInfluenceInfo(commit.name, prevCommit.name, adjCommits)
            dataProcessor.processData(data)
        }
    }
}
