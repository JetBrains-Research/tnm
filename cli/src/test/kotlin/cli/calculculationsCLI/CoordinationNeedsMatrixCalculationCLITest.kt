package cli.calculculationsCLI

import TestConfig.branch
import TestConfig.gitDir
import cli.AbstractCLI
import cli.AbstractCLI.Companion.LONGNAME_ID_TO_FILE
import cli.AbstractCLI.Companion.LONGNAME_ID_TO_USER
import cli.AbstractCLI.Companion.LONGNAME_NUM_THREADS
import cli.AbstractCLITest
import cli.calculculationsCLI.CoordinationNeedsMatrixCalculationCLI.Companion.LONGNAME_COORDINATION_NEEDS
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Test
import util.ProjectConfig
import java.io.File

class CoordinationNeedsMatrixCalculationCLITest : AbstractCLITest(testFolder) {
    companion object {
        private val testFolder = File(tmpCLITestFolder, "CoordinationNeedsMatrixCalculationCLI/")
    }

    private val coordinationNeedsJsonFile = File(testFolder, "CN")

    private val requiredOptions = listOf(
        AbstractCLI.LONGNAME_REPOSITORY to gitDir.absolutePath
    )

    private val nonRequiredOptions = listOf(
        LONGNAME_COORDINATION_NEEDS to coordinationNeedsJsonFile.absolutePath,
        LONGNAME_NUM_THREADS to ProjectConfig.DEFAULT_NUM_THREADS.toString(),
        LONGNAME_ID_TO_USER to idToUserJsonFile.absolutePath,
        LONGNAME_ID_TO_FILE to idToFileJsonFile.absolutePath,
    )

    private val arguments = listOf(
        branch
    )

    @Test
    fun `test json files after run`() {
        val input = createInput(requiredOptions, nonRequiredOptions, arguments)
        CoordinationNeedsMatrixCalculationCLI().parse(input)

        checkIdToEntity(idToUserJsonFile)
        checkIdToEntity(idToFileJsonFile)

        val coordinationNeeds = Json.decodeFromString<Array<FloatArray>>(coordinationNeedsJsonFile.readText())
        assertTrue(coordinationNeeds.isNotEmpty())
    }
}