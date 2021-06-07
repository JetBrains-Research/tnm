package cli.gitMinersCLI

import TestConfig.branch
import TestConfig.gitDir
import cli.AbstractCLI
import cli.AbstractCLI.Companion.LONGNAME_ID_TO_FILE
import cli.AbstractCLI.Companion.LONGNAME_ID_TO_USER
import cli.AbstractCLI.Companion.LONGNAME_NUM_THREADS
import cli.AbstractCLITest
import cli.gitMinersCLI.ChangedFilesMinerCLI.Companion.LONGNAME_CHANGED_FILES_BY_USER
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Test
import util.ProjectConfig
import java.io.File

class UserChangedFilesMinerCLITest : AbstractCLITest(testFolder) {
    companion object {
        private val testFolder = File(tmpCLITestFolder, "ChangedFilesMinerCLITest/")
    }

    private val changedFilesJsonFile = File(testFolder, "CF")

    private val requiredOptions = listOf(
        AbstractCLI.LONGNAME_REPOSITORY to gitDir.absolutePath
    )

    private val nonRequiredOptions = listOf(
        LONGNAME_CHANGED_FILES_BY_USER to changedFilesJsonFile.absolutePath,
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
        ChangedFilesMinerCLI().parse(input)

        checkIdToEntity(idToFileJsonFile)
        checkIdToEntity(idToUserJsonFile)

        val changedFilesMatrix = Json.decodeFromString<Map<Int, Set<Int>>>(changedFilesJsonFile.readText())
        assertTrue(changedFilesMatrix.isNotEmpty())
    }

}