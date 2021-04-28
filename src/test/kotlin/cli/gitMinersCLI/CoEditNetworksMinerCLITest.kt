package cli.gitMinersCLI

import TestConfig.branch
import TestConfig.gitDir
import cli.AbstractCLI.Companion.LONGNAME_ID_TO_COMMIT
import cli.AbstractCLI.Companion.LONGNAME_ID_TO_FILE
import cli.AbstractCLI.Companion.LONGNAME_ID_TO_USER
import cli.AbstractCLI.Companion.LONGNAME_NUM_THREADS
import cli.AbstractCLI.Companion.LONGNAME_REPOSITORY
import cli.AbstractCLITest
import cli.gitMinersCLI.CoEditNetworksMinerCLI.Companion.LONGNAME_CO_EDIT_NETWORKS
import dataProcessor.CoEditNetworksDataProcessor
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Test
import util.ProjectConfig
import java.io.File

class CoEditNetworksMinerCLITest : AbstractCLITest(testFolder) {
    companion object {
        private val testFolder = File(tmpCLITestFolder, "CoEditNetworksMinerCLITest/")
    }

    private val coEditNetworksJsonFile = File(testFolder, "CEN")

    private val requiredOptions = listOf(
        LONGNAME_REPOSITORY to gitDir.absolutePath
    )

    private val nonRequiredOptions = listOf(
        LONGNAME_CO_EDIT_NETWORKS to coEditNetworksJsonFile.absolutePath,
        LONGNAME_NUM_THREADS to ProjectConfig.DEFAULT_NUM_THREADS.toString(),
        LONGNAME_ID_TO_USER to idToUserJsonFile.absolutePath,
        LONGNAME_ID_TO_FILE to idToFileJsonFile.absolutePath,
        LONGNAME_ID_TO_COMMIT to idToCommitJsonFile.absolutePath,
    )
    private val arguments = listOf(
        branch
    )

    @Test
    fun `test json files after run`() {
        val input = createInput(requiredOptions, nonRequiredOptions, arguments)
        CoEditNetworksMinerCLI().parse(input)

        checkIdToEntity(idToFileJsonFile)
        checkIdToEntity(idToUserJsonFile)
        checkIdToEntity(idToCommitJsonFile)

        val coEdit =
            Json.decodeFromString<Set<CoEditNetworksDataProcessor.CommitResult>>(coEditNetworksJsonFile.readText())
        assertTrue(coEdit.isNotEmpty())
    }
}