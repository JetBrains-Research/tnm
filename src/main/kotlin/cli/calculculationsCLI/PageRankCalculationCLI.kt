package cli.calculculationsCLI

import calculations.PageRankCalculation
import cli.InfoCLI
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.float
import util.ProjectConfig

class PageRankCalculationCLI : CalculationCLI(
    InfoCLI(
        "PageRankCalculation",
        "Computes PageRank vector which contains importance rankings (in the bug-fixing and bug creation context) " +
                "for each commit. The computation results are saved to a JSON file ${ProjectConfig.PAGERANK_MATRIX} as " +
                "a vector, where j-th entry corresponds to the importance of a commit."
    )
) {
    private val alpha by option("-a", "--alpha", help = "Scalar parameter in [0, 1]. By default 0.85").float()
        .default(0.85f)

    override fun run() {
        val calculation = PageRankCalculation(resources, alpha)
        calculation.run()
        calculation.saveToJson(resources)
    }
}