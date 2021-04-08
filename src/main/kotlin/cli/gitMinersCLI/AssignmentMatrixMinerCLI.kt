package cli.gitMinersCLI

import cli.InfoCLI
import cli.gitMinersCLI.base.GitMinerMultithreadedMultipleBranchesCLI
import dataProcessor.AssignmentMatrixDataProcessor
import miners.gitMiners.AssignmentMatrixMiner
import util.ProjectConfig
import util.UtilFunctions
import java.io.File

class AssignmentMatrixMinerCLI :
    GitMinerMultithreadedMultipleBranchesCLI
        (
        InfoCLI(
            "AssignmentMatrixMiner",
            "Miner yields a JSON file ${ProjectConfig.ASSIGNMENT_MATRIX} with map of maps, where outer " +
                    "key is the user id, inner key is the file id and the value is the number of times the user has edited the file."
        )
    ) {

    override fun run() {
        val dataProcessor = AssignmentMatrixDataProcessor()
        val miner = AssignmentMatrixMiner(repository, branches, numThreads = numThreads)
        miner.run(dataProcessor)

        UtilFunctions.saveToJson(File(resources, ProjectConfig.ASSIGNMENT_MATRIX), dataProcessor.assignmentMatrix)
        dataProcessor.saveMappersToJson(resources)
    }
}