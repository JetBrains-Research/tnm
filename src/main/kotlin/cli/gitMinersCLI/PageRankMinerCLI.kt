package cli.gitMinersCLI

import cli.InfoCLI
import gitMiners.PageRankMiner

class PageRankMinerCLI : GitMinerCLI(
    InfoCLI(
        "PageRankMiner",
        "Mine data for calculating ranks of bug-fixing commits based on the Google's PageRank algorithm"
    )
) {
    override fun run() {
        val miner = PageRankMiner(repository!!, branches)
        miner.run()
        miner.saveToJson(resources!!)
    }
}