package cli.calculculationsCLI

import calculations.CoordinationNeedsMatrixCalculation
import cli.InfoCLI
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import util.ProjectConfig

class CoordinationNeedsMatrixCalculationCLI : CalculationCLI(
    InfoCLI(
        "CoordinationNeedsMatrixCalculation",
        "Calculation of coordination needed between developers. Needs results from " +
                "AssignmentMatrixMiner and FileDependencyMatrixMiner in resource folder." +
                "The computation results are saved to a JSON file ${ProjectConfig.CN_MATRIX} as a matrix " +
                "C[i][j], where i, j are the developers user ids, and C[i][j] is the relative coordination " +
                "need (in a [0, 1] range) between the two individuals"
    )
) {
    private val resourcesA by option(
        "--resources-A",
        help = "AssignmentMatrixMiner resource directory with results"
    )
        .file(mustExist = true, canBeDir = true, canBeFile = false)
        .required()

    private val resourcesD by option(
        "--resources-D",
        help = "FileDependencyMatrixMiner resource directory with results"
    )
        .file(mustExist = true, canBeDir = true, canBeFile = false)
        .required()

    override fun run() {
        val calculation = CoordinationNeedsMatrixCalculation(resourcesA, resourcesD)
        calculation.run()
        calculation.saveToJson(resources)
    }
}