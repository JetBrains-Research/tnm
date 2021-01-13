package cli.gitMinersCLI

import cli.InfoCLI
import gitMiners.PageRankMiner
import util.ProjectConfig

class PageRankMinerCLI : MultithreadedGitMinerCLI(
    InfoCLI(
        "PageRankMiner",
        "Mine data for calculating ranks of bug-fixing commits based on the Google's PageRank algorithm. " +
                "Result is Adjacency Map of bug-fixing commits and bug-creating commits. " +
                "Output is JSON file ${ProjectConfig.COMMITS_GRAPH}"
    )
) {
    override fun run() {
        val miner = PageRankMiner(repository, branches, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }
}