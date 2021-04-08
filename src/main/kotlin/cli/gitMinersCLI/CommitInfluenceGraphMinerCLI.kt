package cli.gitMinersCLI

import cli.InfoCLI
import cli.gitMinersCLI.base.GitMinerMultithreadedMultipleBranchesCLI
import dataProcessor.CommitInfluenceGraphDataProcessor
import miners.gitMiners.CommitInfluenceGraphMiner
import util.ProjectConfig
import util.UtilFunctions
import java.io.File

class CommitInfluenceGraphMinerCLI : GitMinerMultithreadedMultipleBranchesCLI(
    InfoCLI(
        "CommitInfluenceGraphMiner",
        "Miner yields the JSON file ${ProjectConfig.COMMITS_GRAPH} with a map of lists, with key corresponding to the fixing " +
                "commit id and value corresponding to the commits with lines changed by fixes."
    )
) {
    override fun run() {
        val dataProcessor = CommitInfluenceGraphDataProcessor()
        val miner = CommitInfluenceGraphMiner(repository, branches, numThreads = numThreads)
        miner.run(dataProcessor)

        UtilFunctions.saveToJson(File(resources, ProjectConfig.COMMITS_GRAPH), dataProcessor.adjacencyMap)
        dataProcessor.saveMappersToJson(resources)
    }
}