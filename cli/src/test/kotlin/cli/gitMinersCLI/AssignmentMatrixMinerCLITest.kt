package cli.gitMinersCLI

import TestConfig.branch
import TestConfig.gitDir
import cli.AbstractCLI.Companion.LONGNAME_ID_TO_FILE
import cli.AbstractCLI.Companion.LONGNAME_ID_TO_USER
import cli.AbstractCLI.Companion.LONGNAME_NUM_THREADS
import cli.AbstractCLI.Companion.LONGNAME_REPOSITORY
import cli.AbstractCLITest
import cli.gitMinersCLI.AssignmentMatrixMinerCLI.Companion.LONGNAME_ASSIGNMENT_MATRIX
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Test
import util.ProjectConfig
import java.io.File
import kotlin.test.assertTrue

class AssignmentMatrixMinerCLITest : AbstractCLITest(testFolder) {
    companion object {
        private val testFolder = File(tmpCLITestFolder, "AssignmentMatrixCLITest/")
    }

    private val assignmentMatrixJsonFile = File(testFolder, "AM")

    private val requiredOptions = listOf(
        LONGNAME_REPOSITORY to gitDir.absolutePath
    )

    private val nonRequiredOptions = listOf(
        LONGNAME_ASSIGNMENT_MATRIX to assignmentMatrixJsonFile.absolutePath,
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
        AssignmentMatrixMinerCLI().parse(input)

        checkIdToEntity(idToFileJsonFile)
        checkIdToEntity(idToUserJsonFile)

        val assignmentMatrix = Json.decodeFromString<Map<Int, Map<Int, Int>>>(assignmentMatrixJsonFile.readText())
        assertTrue(assignmentMatrix.isNotEmpty())
    }

}