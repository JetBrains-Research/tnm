package cli.gitMinersCLI

import cli.InfoCLI
import cli.gitMinersCLI.base.GitMinerMultithreadedOneBranchCLI
import gitMiners.CoEditNetworksMiner
import util.ProjectConfig

class CoEditNetworksMinerCLI : GitMinerMultithreadedOneBranchCLI(
    InfoCLI(
        "CoEditNetworksMiner",
        "Miner yields JSON file ${ProjectConfig.CO_EDIT} with dict of commits information and list of edits. " +
                "Each edit includes pre/post file path, start line, length, number of chars, " +
                "entropy of changed block of code, Levenshtein distance between previous and new block of code, type of edit."
    )
) {
    override fun run() {
//        val miner = CoEditNetworksMiner(repository, branch, numThreads = numThreads)
//        miner.run()
//        miner.saveToJson(resources)
    }
}