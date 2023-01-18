package cli.calculculationsCLI

import calculations.CoordinationNeedsMatrixCalculation
import cli.AbstractRepoCLI
import dataProcessor.AssignmentMatrixDataProcessor
import dataProcessor.FileDependencyMatrixDataProcessor
import miners.gitMiners.FilesChangesetMiner
import miners.gitMiners.UserChangedFilesMiner
import util.HelpFunctionsUtil
import java.io.File

class CoordinationNeedsMatrixCalculationCLI : AbstractRepoCLI(
    "CoordinationNeedsMatrixCalculation",
    "Calculation of coordination needed between developers. Needs results from " +
        "AssignmentMatrixMiner and FileDependencyMatrixMiner in resource folder." +
        "The computation results are saved to a $HELP_COORDINATION_NEEDS."
) {

    companion object {
        const val HELP_COORDINATION_NEEDS = "JSON file as a matrix " +
            "C[i][j], where i, j are the developers user ids, and C[i][j] is the relative coordination " +
            "need (in a [0, 1] range) between the two individuals"
        const val LONGNAME_COORDINATION_NEEDS = "--coordination-needs"
    }

    private val coordinationNeedsJsonFile by saveFileOption(
        LONGNAME_COORDINATION_NEEDS,
        HELP_COORDINATION_NEEDS,
        File(resultDir, "CoordinationNeeds")
    )

    private val branches by branchesOption()
    private val numOfThreads by numOfThreadsOption()
    private val idToUserJsonFile by idToUserOption()
    private val idToFileJsonFile by idToFileOption()

    override fun run() {
        val assignmentMatrixDataProcessor = AssignmentMatrixDataProcessor()
        val userChangedFilesMiner = UserChangedFilesMiner(repositoryDirectory, branches, numThreads = numOfThreads)
        userChangedFilesMiner.run(assignmentMatrixDataProcessor)
        val numOfUsers = assignmentMatrixDataProcessor.idToUser.size

        val fileDependencyDataProcessor = FileDependencyMatrixDataProcessor()
        val filesChangesetMiner = FilesChangesetMiner(repositoryDirectory, branches, numThreads = 1)
        filesChangesetMiner.run(fileDependencyDataProcessor)

        val numOfFiles = fileDependencyDataProcessor.idToFile.size
        val fileDependencyMatrix = HelpFunctionsUtil.convertLowerTriangleMapTo1dArray(
            HelpFunctionsUtil.changeKeysInMapOfMaps(
                fileDependencyDataProcessor.counter.toMap(),
                fileDependencyDataProcessor.idToFile, assignmentMatrixDataProcessor.fileToId,
                fileDependencyDataProcessor.idToFile, assignmentMatrixDataProcessor.fileToId

            ),
            numOfFiles,
            numOfFiles
        )

        val assignmentMatrix = HelpFunctionsUtil.convertMapTo1dArray(
            assignmentMatrixDataProcessor.assignmentMatrix,
            numOfUsers,
            numOfFiles
        )

        val coordinationNeeds = CoordinationNeedsMatrixCalculation.run(
            fileDependencyMatrix,
            assignmentMatrix,
            numOfUsers,
            numOfFiles
        )

        HelpFunctionsUtil.saveToJson(
            coordinationNeedsJsonFile,
            coordinationNeeds
        )

        HelpFunctionsUtil.saveToJson(
            idToUserJsonFile,
            assignmentMatrixDataProcessor.idToUser
        )

        HelpFunctionsUtil.saveToJson(
            idToFileJsonFile,
            assignmentMatrixDataProcessor.idToFile
        )
    }
}
