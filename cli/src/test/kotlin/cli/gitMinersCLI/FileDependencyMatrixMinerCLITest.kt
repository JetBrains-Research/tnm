package cli.gitMinersCLI

import TestConfig.branch
import TestConfig.gitDir
import cli.AbstractCLI.Companion.LONGNAME_ID_TO_FILE
import cli.AbstractCLI.Companion.LONGNAME_NUM_THREADS
import cli.AbstractCLI.Companion.LONGNAME_REPOSITORY
import cli.AbstractCLITest
import cli.gitMinersCLI.FileDependencyMatrixMinerCLI.Companion.LONGNAME_FILE_DEPENDENCY_MATRIX
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Test
import util.ProjectConfig
import java.io.File

class FileDependencyMatrixMinerCLITest : AbstractCLITest(testFolder) {
    companion object {
        private val testFolder = File(tmpCLITestFolder, "FileDependencyMatrixMinerCLITest/")
    }

    private val fileDependencyMatrixJsonFile = File(testFolder, "FDM")

    private val requiredOptions = listOf(
        LONGNAME_REPOSITORY to gitDir.absolutePath
    )

    private val nonRequiredOptions = listOf(
        LONGNAME_FILE_DEPENDENCY_MATRIX to fileDependencyMatrixJsonFile.absolutePath,
        LONGNAME_ID_TO_FILE to idToFileJsonFile.absolutePath,
    )

    private val arguments = listOf(
        branch
    )

    @Test
    fun `test json files after run`() {
        val input = createInput(requiredOptions, nonRequiredOptions, arguments)
        FileDependencyMatrixMinerCLI().parse(input)

        checkIdToEntity(idToFileJsonFile)

        val fileDependencyMatrix =
            Json.decodeFromString<Map<Int, Map<Int, Int>>>(fileDependencyMatrixJsonFile.readText())
        assertTrue(fileDependencyMatrix.isNotEmpty())
    }

}