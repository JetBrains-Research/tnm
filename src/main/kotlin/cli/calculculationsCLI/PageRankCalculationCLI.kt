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
        "Calculation of ranks of bug-fixing commits based on the Google's PageRank algorithm. " +
                "Needs results from PageRankMiner in resource directory. Output is vector of PageRank values for each " +
                "commit in JSON file named as ${ProjectConfig.PAGERANK_MATRIX}"
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