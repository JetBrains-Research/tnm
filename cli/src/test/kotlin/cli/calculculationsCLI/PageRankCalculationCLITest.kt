package cli.calculculationsCLI

import TestConfig.branch
import TestConfig.gitDir
import calculations.PageRankCalculation
import cli.AbstractCLI
import cli.AbstractCLITest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Test
import util.ProjectConfig
import java.io.File

class PageRankCalculationCLITest : AbstractCLITest(testFolder) {
    companion object {
        private val testFolder = File(tmpCLITestFolder, "PageRankCalculationTest/")
    }

    private val pageRankJsonFile = File(testFolder, "PR")

    private val requiredOptions = listOf(
        AbstractCLI.LONGNAME_REPOSITORY to gitDir.absolutePath
    )

    private val nonRequiredOptions = listOf(
        PageRankCalculationCLI.LONGNAME_ALPHA to PageRankCalculation.DEFAULT_ALPHA.toString(),
        PageRankCalculationCLI.LONGNAME_PAGE_RANK to pageRankJsonFile.absolutePath,
        AbstractCLI.LONGNAME_NUM_THREADS to ProjectConfig.DEFAULT_NUM_THREADS.toString(),
        AbstractCLI.LONGNAME_ID_TO_COMMIT to idToCommitJsonFile.absolutePath,
    )

    private val arguments = listOf(
        branch
    )

    @Test
    fun `test json files after run`() {
        val input = createInput(requiredOptions, nonRequiredOptions, arguments)
        PageRankCalculationCLI().parse(input)

        checkIdToEntity(idToCommitJsonFile)

        val pageRank = Json.decodeFromString<Map<Int, Double>>(pageRankJsonFile.readText())
        assertTrue(pageRank.isNotEmpty())
    }
}