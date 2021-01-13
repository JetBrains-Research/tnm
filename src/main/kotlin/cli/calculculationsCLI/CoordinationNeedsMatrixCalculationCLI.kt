package cli.calculculationsCLI

import calculations.CoordinationNeedsMatrixCalculation
import cli.InfoCLI
import util.ProjectConfig

class CoordinationNeedsMatrixCalculationCLI : CalculationCLI(
    InfoCLI(
        "CoordinationNeedsMatrixCalculation",
        "Calculation of coordination needed between developers. Needs results from " +
                "AssignmentMatrixMiner and FileDependencyMatrixMiner in resource folder." +
                "Output is 2d coordination needs matrix between developers in JSON file named as ${ProjectConfig.CN_MATRIX}."
    )
) {
    override fun run() {
        val calculation = CoordinationNeedsMatrixCalculation(resources)
        calculation.run()
        calculation.saveToJson(resources)
    }
}