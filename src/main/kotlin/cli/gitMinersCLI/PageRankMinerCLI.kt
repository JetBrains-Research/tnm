package cli.gitMinersCLI

import cli.InfoCLI
import gitMiners.PageRankMiner

class PageRankMinerCLI : MultithreadedGitMinerCLI(
    InfoCLI(
        "PageRankMiner",
        "Mine data for calculating ranks of bug-fixing commits based on the Google's PageRank algorithm"
    )
) {
    override fun run() {
        val miner = PageRankMiner(repository!!, branches, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources!!)
    }
}