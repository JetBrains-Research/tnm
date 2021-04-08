package cli.gitMinersCLI

import cli.InfoCLI
import cli.gitMinersCLI.base.GitMinerMultithreadedMultipleBranchesCLI
import dataProcessor.ChangedFilesDataProcessor
import miners.gitMiners.ChangedFilesMiner
import util.ProjectConfig
import util.UtilFunctions
import java.io.File

class ChangedFilesMinerCLI : GitMinerMultithreadedMultipleBranchesCLI(
    InfoCLI(
        "ChangedFilesMiner",
        "Miner yields a JSON file ${ProjectConfig.USER_FILES_IDS} with a map, where key is the user id of " +
                "a developer, and value is the list of file ids for the files edited by a developer."
    )
) {
    override fun run() {
        val dataProcessor = ChangedFilesDataProcessor()
        val miner = ChangedFilesMiner(repository, branches, numThreads = numThreads)
        miner.run(dataProcessor)

        UtilFunctions.saveToJson(File(resources, ProjectConfig.USER_FILES_IDS), dataProcessor.userFilesIds)
        UtilFunctions.saveToJsonDataProcessorMaps(resources, dataProcessor)
    }
}