package cli.gitMinersCLI

import TestConfig.branch
import TestConfig.gitDir
import cli.AbstractCLI.Companion.LONGNAME_ID_TO_COMMIT
import cli.AbstractCLI.Companion.LONGNAME_NUM_THREADS
import cli.AbstractCLI.Companion.LONGNAME_REPOSITORY
import cli.AbstractCLITest
import cli.gitMinersCLI.CommitInfluenceGraphMinerCLI.Companion.LONGNAME_COMMITS_GRAPH
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Test
import util.ProjectConfig
import java.io.File

class CommitInfluenceGraphMinerCLITest : AbstractCLITest(testFolder) {
    companion object {
        private val testFolder = File(tmpCLITestFolder, "CommitInfluenceGraphMinerCLITest/")
    }

    private val adjacencyMapJsonFile = File(testFolder, "CIG")

    val requiredOptions = listOf(
        LONGNAME_REPOSITORY to gitDir.absolutePath
    )

    val nonRequiredOptions = listOf(
        LONGNAME_COMMITS_GRAPH to adjacencyMapJsonFile.absolutePath,
        LONGNAME_NUM_THREADS to ProjectConfig.DEFAULT_NUM_THREADS.toString(),
        LONGNAME_ID_TO_COMMIT to idToCommitJsonFile.absolutePath,
    )

    val arguments = listOf(
        branch
    )

    @Test
    fun `test json files after run`() {
        val input = createInput(requiredOptions, nonRequiredOptions, arguments)
        CommitInfluenceGraphMinerCLI().parse(input)

        checkIdToEntity(idToCommitJsonFile)
        val adjacencyMap = Json.decodeFromString<Map<Int, Set<Int>>>(adjacencyMapJsonFile.readText())
        assertTrue(adjacencyMap.isNotEmpty())
    }

}