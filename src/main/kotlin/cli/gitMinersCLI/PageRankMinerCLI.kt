package cli.gitMinersCLI

import cli.InfoCLI
import gitMiners.PageRankMiner
import util.ProjectConfig

class PageRankMinerCLI : MultithreadedGitMinerCLI(
    InfoCLI(
        "PageRankMiner",
        "Miner yields the JSON file ${ProjectConfig.COMMITS_GRAPH} with a map of lists, with key corresponding to the fixing " +
                "commit id and value corresponding to the commits with lines changed by fixes."
    )
) {
    override fun run() {
        val miner = PageRankMiner(repository, branches, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }
}