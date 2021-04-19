package cli.gitMinersCLI

import cli.InfoCLI
import cli.gitMinersCLI.base.GitMinerMultithreadedOneBranchCLI
import dataProcessor.CoEditNetworksDataProcessor
import miners.gitMiners.CoEditNetworksMiner
import util.ProjectConfig
import util.UtilFunctions
import java.io.File

class CoEditNetworksMinerCLI : GitMinerMultithreadedOneBranchCLI(
    InfoCLI(
        "CoEditNetworksMiner",
        "Miner yields JSON file ${ProjectConfig.CO_EDIT} with dict of commits information and list of edits. " +
                "Each edit includes pre/post file path, start line, length, number of chars, " +
                "entropy of changed block of code, Levenshtein distance between previous and new block of code, type of edit."
    )
) {
    override fun run() {
        val dataProcessor = CoEditNetworksDataProcessor()
        val miner = CoEditNetworksMiner(repository, branch, numThreads = numThreads)
        miner.run(dataProcessor)

        UtilFunctions.saveToJson(File(resources, ProjectConfig.CO_EDIT), dataProcessor.coEdits)
        UtilFunctions.saveToJsonDataProcessorMaps(resources, dataProcessor)
    }
}