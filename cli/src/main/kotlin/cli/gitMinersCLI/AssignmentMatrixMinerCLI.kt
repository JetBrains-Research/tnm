package cli.gitMinersCLI

import cli.gitMinersCLI.base.GitMinerMultithreadedMultipleBranchesCLI
import dataProcessor.AssignmentMatrixDataProcessor
import miners.gitMiners.AssignmentMatrixMiner
import org.eclipse.jgit.internal.storage.file.FileRepository
import util.HelpFunctionsUtil
import java.io.File

class AssignmentMatrixMinerCLI :
    GitMinerMultithreadedMultipleBranchesCLI
        (
        "AssignmentMatrixMiner",
        "Miner yields a $HELP_ASSIGNMENT_MATRIX"
    ) {

    companion object {
        const val HELP_ASSIGNMENT_MATRIX =
            "JSON file with map of maps, where outer key is the user id, " +
                    "inner key is the file id and the value is the number of times the user has edited the file."
        const val LONGNAME_ASSIGNMENT_MATRIX = "--assignment-matrix"
    }

    private val assignmentMatrixJsonFile by saveFileOption(
        LONGNAME_ASSIGNMENT_MATRIX,
        HELP_ASSIGNMENT_MATRIX,
        File(resultDir, "AssignmentMatrix")
    )

    private val idToUserJsonFile by idToUserOption()
    private val idToFileJsonFile by idToFileOption()

    override fun run() {
        val repository = FileRepository(repositoryDirectory)
        val dataProcessor = AssignmentMatrixDataProcessor()
        val miner = AssignmentMatrixMiner(repository, branches, numThreads = numThreads)
        miner.run(dataProcessor)

        HelpFunctionsUtil.saveToJson(
            assignmentMatrixJsonFile,
            dataProcessor.assignmentMatrix
        )

        HelpFunctionsUtil.saveToJson(
            idToUserJsonFile,
            dataProcessor.idToUser
        )

        HelpFunctionsUtil.saveToJson(
            idToFileJsonFile,
            dataProcessor.idToFile
        )
    }
}
