package calculations

import TestConfig.branch
import TestConfig.gitDir
import cli.AbstractCLI.Companion.LONGNAME_ID_TO_COMMIT
import cli.AbstractCLI.Companion.LONGNAME_NUM_THREADS
import cli.AbstractCLI.Companion.LONGNAME_REPOSITORY
import cli.AbstractCLITest
import cli.calculculationsCLI.PageRankCalculationCLI
import cli.calculculationsCLI.PageRankCalculationCLI.Companion.LONGNAME_ALPHA
import cli.calculculationsCLI.PageRankCalculationCLI.Companion.LONGNAME_PAGE_RANK
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import util.ProjectConfig
import java.io.File

class PageRankCalculationTest: AbstractCLITest(testFolder) {
    companion object {
        private val testFolder = File(tmpCLITestFolder, "PageRankCalculationTest/")
    }

    private val pageRankJsonFile = File(testFolder, "PR")

    private val requiredOptions = listOf(
        LONGNAME_REPOSITORY to gitDir.absolutePath
    )

    private val nonRequiredOptions = listOf(
        LONGNAME_ALPHA to PageRankCalculation.DEFAULT_ALPHA.toString(),
        LONGNAME_PAGE_RANK to pageRankJsonFile.absolutePath,
        LONGNAME_NUM_THREADS to ProjectConfig.DEFAULT_NUM_THREADS.toString(),
        LONGNAME_ID_TO_COMMIT to idToCommitJsonFile.absolutePath,
    )

    private val arguments = listOf(
        branch
    )

    @Test
    fun `test json files after run`() {
        val input = createInput(requiredOptions, nonRequiredOptions, arguments)
        PageRankCalculationCLI().parse(input)

        checkIdToEntity(idToCommitJsonFile)

        val pageRank = Json.decodeFromString<Array<FloatArray>>(pageRankJsonFile.readText())
        assertTrue(pageRank.isNotEmpty())
    }
}