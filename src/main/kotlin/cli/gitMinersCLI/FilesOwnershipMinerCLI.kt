package cli.gitMinersCLI

import cli.InfoCLI
import cli.gitMinersCLI.base.GitMinerMultithreadedOneBranchCLI
import dataProcessor.FilesOwnershipDataProcessor
import miners.gitMiners.FilesOwnershipMiner
import util.ProjectConfig
import util.UtilFunctions
import java.io.File

class FilesOwnershipMinerCLI : GitMinerMultithreadedOneBranchCLI(
    // TODO: change help, add info about other maps
    InfoCLI(
        "FilesOwnershipMiner",
        "Miner yields JSON file ${ProjectConfig.DEVELOPER_KNOWLEDGE} with map of maps, where the outer " +
                "key is the user id, the inner key is the file id and the value is the developer knowledge about the file."
    )
) {

    override fun run() {
        val dataProcessor = FilesOwnershipDataProcessor()
        val miner = FilesOwnershipMiner(repository, numThreads = numThreads)
        miner.run(dataProcessor)

        UtilFunctions.saveToJson(File(resources, ProjectConfig.DEVELOPER_KNOWLEDGE), dataProcessor.developerKnowledge)
        UtilFunctions.saveToJson(File(resources, ProjectConfig.FILES_OWNERSHIP), dataProcessor.filesOwnership)
        UtilFunctions.saveToJson(File(resources, ProjectConfig.POTENTIAL_OWNERSHIP), dataProcessor.potentialAuthorship)

        UtilFunctions.saveToJsonDataProcessorMaps(resources, dataProcessor)
    }
}