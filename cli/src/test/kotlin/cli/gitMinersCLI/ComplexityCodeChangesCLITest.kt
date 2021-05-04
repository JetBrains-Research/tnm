package cli.gitMinersCLI

import TestConfig.branch
import TestConfig.gitDir
import cli.AbstractCLI
import cli.AbstractCLI.Companion.LONGNAME_ID_TO_FILE
import cli.AbstractCLI.Companion.LONGNAME_NUM_THREADS
import cli.AbstractCLITest
import cli.gitMinersCLI.ComplexityCodeChangesCLI.Companion.LONGNAME_CHANGE_TYPE
import cli.gitMinersCLI.ComplexityCodeChangesCLI.Companion.LONGNAME_COMPLEXITY_CODE_CHANGES
import cli.gitMinersCLI.ComplexityCodeChangesCLI.Companion.LONGNAME_NUM_OF_COMMITS
import cli.gitMinersCLI.ComplexityCodeChangesCLI.Companion.LONGNAME_NUM_OF_MONTH
import cli.gitMinersCLI.ComplexityCodeChangesCLI.Companion.LONGNAME_PERIOD_TYPE
import dataProcessor.ComplexityCodeChangesDataProcessor.*
import dataProcessor.ComplexityCodeChangesDataProcessor.Companion.DEFAULT_NUM_COMMITS
import dataProcessor.ComplexityCodeChangesDataProcessor.Companion.DEFAULT_NUM_MONTH
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Test
import util.ProjectConfig
import java.io.File

class ComplexityCodeChangesCLITest : AbstractCLITest(testFolder) {
    companion object {
        private val testFolder = File(tmpCLITestFolder, "ComplexityCodeChangesCLITest/")
    }

    private val complexityCodeChangesJsonFile = File(testFolder, "CCC")

    private fun runTest(
        requiredOptions: List<Pair<String, String>>,
        nonRequiredOptions: List<Pair<String, String>>,
        arguments: List<String>
    ) {
        val input = createInput(requiredOptions, nonRequiredOptions, arguments)
        ComplexityCodeChangesCLI().parse(input)

        checkIdToEntity(idToFileJsonFile)

        val complexityCodeChanges =
            Json.decodeFromString<Map<Int, PeriodStats>>(complexityCodeChangesJsonFile.readText())
        assertTrue(complexityCodeChanges.isNotEmpty())
    }

    @Test
    fun `test time based and lines`() {
        val requiredOptions = listOf(
            AbstractCLI.LONGNAME_REPOSITORY to gitDir.absolutePath
        )

        val nonRequiredOptions = listOf(
            LONGNAME_NUM_OF_MONTH to DEFAULT_NUM_MONTH.toString(),
            LONGNAME_CHANGE_TYPE to ChangeType.LINES.toString(),
            LONGNAME_PERIOD_TYPE to PeriodType.TIME_BASED.toString(),
            LONGNAME_COMPLEXITY_CODE_CHANGES to complexityCodeChangesJsonFile.absolutePath,
            LONGNAME_NUM_THREADS to ProjectConfig.DEFAULT_NUM_THREADS.toString(),
            LONGNAME_ID_TO_FILE to idToFileJsonFile.absolutePath,
        )

        val arguments = listOf(
            branch
        )

        runTest(requiredOptions, nonRequiredOptions, arguments)
    }

    @Test
    fun `test time based and files`() {
        val requiredOptions = listOf(
            AbstractCLI.LONGNAME_REPOSITORY to gitDir.absolutePath
        )

        val nonRequiredOptions = listOf(
            LONGNAME_NUM_OF_MONTH to DEFAULT_NUM_MONTH.toString(),
            LONGNAME_CHANGE_TYPE to ChangeType.FILE.toString(),
            LONGNAME_PERIOD_TYPE to PeriodType.TIME_BASED.toString(),
            LONGNAME_COMPLEXITY_CODE_CHANGES to complexityCodeChangesJsonFile.absolutePath,
            LONGNAME_NUM_THREADS to ProjectConfig.DEFAULT_NUM_THREADS.toString(),
            LONGNAME_ID_TO_FILE to idToFileJsonFile.absolutePath,
        )

        val arguments = listOf(
            branch
        )

        runTest(requiredOptions, nonRequiredOptions, arguments)
    }

    @Test
    fun `test modification based and lines`() {
        val requiredOptions = listOf(
            AbstractCLI.LONGNAME_REPOSITORY to gitDir.absolutePath
        )

        val nonRequiredOptions = listOf(
            LONGNAME_NUM_OF_COMMITS to DEFAULT_NUM_COMMITS.toString(),
            LONGNAME_CHANGE_TYPE to ChangeType.LINES.toString(),
            LONGNAME_PERIOD_TYPE to PeriodType.MODIFICATION_LIMIT.toString(),
            LONGNAME_COMPLEXITY_CODE_CHANGES to complexityCodeChangesJsonFile.absolutePath,
            LONGNAME_NUM_THREADS to ProjectConfig.DEFAULT_NUM_THREADS.toString(),
            LONGNAME_ID_TO_FILE to idToFileJsonFile.absolutePath,
        )

        val arguments = listOf(
            branch
        )

        runTest(requiredOptions, nonRequiredOptions, arguments)
    }

    @Test
    fun `test modification based and files`() {
        val requiredOptions = listOf(
            AbstractCLI.LONGNAME_REPOSITORY to gitDir.absolutePath
        )

        val nonRequiredOptions = listOf(
            LONGNAME_NUM_OF_COMMITS to DEFAULT_NUM_COMMITS.toString(),
            LONGNAME_CHANGE_TYPE to ChangeType.FILE.toString(),
            LONGNAME_PERIOD_TYPE to PeriodType.MODIFICATION_LIMIT.toString(),
            LONGNAME_COMPLEXITY_CODE_CHANGES to complexityCodeChangesJsonFile.absolutePath,
            LONGNAME_NUM_THREADS to ProjectConfig.DEFAULT_NUM_THREADS.toString(),
            LONGNAME_ID_TO_FILE to idToFileJsonFile.absolutePath,
        )

        val arguments = listOf(
            branch
        )

        runTest(requiredOptions, nonRequiredOptions, arguments)
    }

}