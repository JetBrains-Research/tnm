package cli.calculculationsCLI

import calculations.CNMatrixCalculation
import cli.InfoCLI

class CNMatrixCalculationCLI : CalculationCLI(
    InfoCLI(
        "CNMatrixCalculation",
        "Calculation of needed congruence between developers"
    )
) {
    override fun run() {
        val calculation = CNMatrixCalculation(resources!!)
        calculation.run()
        calculation.saveToJson(resources!!)
    }
}