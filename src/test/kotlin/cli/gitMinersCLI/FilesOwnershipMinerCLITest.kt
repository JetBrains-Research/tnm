package cli.gitMinersCLI

import TestConfig.branch
import TestConfig.gitDir
import cli.AbstractCLI.Companion.LONGNAME_ID_TO_FILE
import cli.AbstractCLI.Companion.LONGNAME_ID_TO_USER
import cli.AbstractCLI.Companion.LONGNAME_NUM_THREADS
import cli.AbstractCLI.Companion.LONGNAME_REPOSITORY
import cli.AbstractCLITest
import cli.gitMinersCLI.FilesOwnershipMinerCLI.Companion.LONGNAME_DEVELOPER_KNOWLEDGE
import cli.gitMinersCLI.FilesOwnershipMinerCLI.Companion.LONGNAME_FILES_OWNERSHIP
import cli.gitMinersCLI.FilesOwnershipMinerCLI.Companion.LONGNAME_POTENTIAL_OWNERSHIP
import dataProcessor.FilesOwnershipDataProcessor.UserData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Test
import util.ProjectConfig
import java.io.File

class FilesOwnershipMinerCLITest : AbstractCLITest(testFolder) {
    companion object {
        private val testFolder = File(tmpCLITestFolder, "FilesOwnershipMinerCLITest/")
    }

    private val filesOwnershipJsonFile = File(testFolder, "FO")
    private val developerKnowledgeJsonFile = File(testFolder, "DK")
    private val potentialOwnershipJsonFile = File(testFolder, "PO")

    private val requiredOptions = listOf(
        LONGNAME_REPOSITORY to gitDir.absolutePath
    )

    private val nonRequiredOptions = listOf(
        LONGNAME_FILES_OWNERSHIP to filesOwnershipJsonFile.absolutePath,
        LONGNAME_POTENTIAL_OWNERSHIP to potentialOwnershipJsonFile.absolutePath,
        LONGNAME_DEVELOPER_KNOWLEDGE to developerKnowledgeJsonFile.absolutePath,
        LONGNAME_NUM_THREADS to ProjectConfig.DEFAULT_NUM_THREADS.toString(),
        LONGNAME_ID_TO_FILE to idToFileJsonFile.absolutePath,
        LONGNAME_ID_TO_USER to idToUserJsonFile.absolutePath,
    )

    private val arguments = listOf(
        branch
    )

    @Test
    fun `test json files after run`() {
        val input = createInput(requiredOptions, nonRequiredOptions, arguments)
        FilesOwnershipMinerCLI().parse(input)

        checkIdToEntity(idToFileJsonFile)
        checkIdToEntity(idToUserJsonFile)

        val filesOwnership = Json.decodeFromString<Map<Int, Map<Int, UserData>>>(filesOwnershipJsonFile.readText())
        assertTrue(filesOwnership.isNotEmpty())

        val developerKnowledge =
            Json.decodeFromString<Map<Int, Map<Int, Double>>>(developerKnowledgeJsonFile.readText())
        assertTrue(developerKnowledge.isNotEmpty())

        val potentialOwnership = Json.decodeFromString<Map<Int, Int>>(potentialOwnershipJsonFile.readText())
        assertTrue(potentialOwnership.isNotEmpty())
    }
}