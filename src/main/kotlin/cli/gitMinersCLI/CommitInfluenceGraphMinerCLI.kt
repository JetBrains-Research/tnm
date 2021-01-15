package cli.gitMinersCLI

import cli.InfoCLI
import gitMiners.CommitInfluenceGraphMiner
import util.ProjectConfig

class CommitInfluenceGraphMinerCLI : MultithreadedGitMinerCLI(
    InfoCLI(
        "CommitInfluenceGraphMiner",
        "Miner yields the JSON file ${ProjectConfig.COMMITS_GRAPH} with a map of lists, with key corresponding to the fixing " +
                "commit id and value corresponding to the commits with lines changed by fixes."
    )
) {
    override fun run() {
        val miner = CommitInfluenceGraphMiner(repository, branches, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }
}