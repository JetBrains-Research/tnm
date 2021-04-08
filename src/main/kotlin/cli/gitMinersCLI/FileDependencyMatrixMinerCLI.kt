package cli.gitMinersCLI

import cli.InfoCLI
import cli.gitMinersCLI.base.GitMinerMultithreadedMultipleBranchesCLI
import dataProcessor.FileDependencyMatrixDataProcessor
import miners.gitMiners.FileDependencyMatrixMiner
import util.ProjectConfig
import util.UtilFunctions
import java.io.File

class FileDependencyMatrixMinerCLI :
    GitMinerMultithreadedMultipleBranchesCLI(
        InfoCLI(
            "FileDependencyMatrixMiner",
            "Miner yields a JSON file ${ProjectConfig.FILE_DEPENDENCY} with map of maps, where both inner and outer " +
                    "keys are file ids and the value is the number of times both file has been edited in the same commit."
        )
    ) {

    override fun run() {
        val dataProcessor = FileDependencyMatrixDataProcessor()
        val miner = FileDependencyMatrixMiner(repository, branches, numThreads = numThreads)
        miner.run(dataProcessor)

        UtilFunctions.saveToJson(File(resources, ProjectConfig.FILE_DEPENDENCY), dataProcessor.fileDependencyMatrix)
        dataProcessor.saveMappersToJson(resources)
    }
}