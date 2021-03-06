package cli.calculculationsCLI

import calculations.CoordinationNeedsMatrixCalculation
import dataProcessor.AssignmentMatrixDataProcessor
import dataProcessor.FileDependencyMatrixDataProcessor
import miners.gitMiners.AssignmentMatrixMiner
import miners.gitMiners.FileDependencyMatrixMiner
import org.eclipse.jgit.internal.storage.file.FileRepository
import util.HelpFunctionsUtil
import java.io.File

class CoordinationNeedsMatrixCalculationCLI : CalculationCLI(
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
        val repository = FileRepository(repositoryDirectory)
        val assignmentMatrixDataProcessor = AssignmentMatrixDataProcessor()
        val assignmentMatrixMiner = AssignmentMatrixMiner(repository, branches, numThreads = numOfThreads)
        assignmentMatrixMiner.run(assignmentMatrixDataProcessor)
        val numOfUsers = assignmentMatrixDataProcessor.idToUser.size

        val fileDependencyDataProcessor = FileDependencyMatrixDataProcessor()
        val fileDependencyMiner = FileDependencyMatrixMiner(repository, branches, numThreads = numOfThreads)
        fileDependencyMiner.run(fileDependencyDataProcessor)

        val numOfFiles = fileDependencyDataProcessor.idToFile.size
        val fileDependencyMatrix = HelpFunctionsUtil.convertMapToArray(
            HelpFunctionsUtil.changeKeysInMapOfMaps(
                fileDependencyDataProcessor.fileDependencyMatrix,
                fileDependencyDataProcessor.idToFile, assignmentMatrixDataProcessor.fileToId,
                fileDependencyDataProcessor.idToFile, assignmentMatrixDataProcessor.fileToId

            ),
            numOfFiles,
            numOfFiles
        )

        val assignmentMatrix = HelpFunctionsUtil.convertMapToArray(
            assignmentMatrixDataProcessor.assignmentMatrix,
            numOfUsers,
            numOfFiles
        )

        val calculation = CoordinationNeedsMatrixCalculation(
            fileDependencyMatrix,
            assignmentMatrix
        )
        calculation.run()

        HelpFunctionsUtil.saveToJson(
            coordinationNeedsJsonFile,
            calculation.coordinationNeeds
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
