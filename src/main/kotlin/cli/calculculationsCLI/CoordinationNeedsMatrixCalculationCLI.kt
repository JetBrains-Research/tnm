package cli.calculculationsCLI

import calculations.CoordinationNeedsMatrixCalculation
import cli.InfoCLI
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
    override fun run() {
        val calculation = CoordinationNeedsMatrixCalculation(resources)
        calculation.run()
        calculation.saveToJson(resources)
    }
}