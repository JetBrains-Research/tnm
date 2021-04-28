package cli.gitMinersCLI

import TestConfig.branch
import TestConfig.gitDir
import cli.AbstractCLI
import cli.AbstractCLI.Companion.LONGNAME_ID_TO_USER
import cli.AbstractCLI.Companion.LONGNAME_NUM_THREADS
import cli.AbstractCLITest
import cli.gitMinersCLI.WorkTimeMinerCLI.Companion.LONGNAME_WORK_TIME
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Test
import util.ProjectConfig
import java.io.File

class WorkTimeMinerCLITest : AbstractCLITest(testFolder) {
    companion object {
        private val testFolder = File(tmpCLITestFolder, "WorkTimeMinerCLITest/")
    }

    private val workTimeJsonFile = File(testFolder, "WT")

    private val requiredOptions = listOf(
        AbstractCLI.LONGNAME_REPOSITORY to gitDir.absolutePath
    )

    private val nonRequiredOptions = listOf(
        LONGNAME_WORK_TIME to workTimeJsonFile.absolutePath,
        LONGNAME_NUM_THREADS to ProjectConfig.DEFAULT_NUM_THREADS.toString(),
        LONGNAME_ID_TO_USER to idToUserJsonFile.absolutePath,
    )

    private val arguments = listOf(
        branch
    )

    @Test
    fun `test json files after run`() {
        val input = createInput(requiredOptions, nonRequiredOptions, arguments)
        WorkTimeMinerCLI().parse(input)

        checkIdToEntity(idToUserJsonFile)

        val workTime = Json.decodeFromString<Map<Int, Map<Int, Int>>>(workTimeJsonFile.readText())
        assertTrue(workTime.isNotEmpty())
    }
}