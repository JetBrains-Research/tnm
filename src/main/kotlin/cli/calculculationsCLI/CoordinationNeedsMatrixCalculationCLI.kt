package cli.calculculationsCLI

import calculations.CoordinationNeedsMatrixCalculation
import cli.InfoCLI

class CoordinationNeedsMatrixCalculationCLI : CalculationCLI(
    InfoCLI(
        "CoordinationNeedsMatrixCalculation",
        "Calculation of needed congruence between developers"
    )
) {
    override fun run() {
        val calculation = CoordinationNeedsMatrixCalculation(resources!!)
        calculation.run()
        calculation.saveToJson(resources!!)
    }
}