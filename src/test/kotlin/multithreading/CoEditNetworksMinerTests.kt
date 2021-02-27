package multithreading

import GitMinerTest
import GitMinerTest.Companion.repositoryDir
import GitMinerTest.Companion.resourcesMultithreadingDir
import GitMinerTest.Companion.resourcesOneThreadDir
import gitMiners.CoEditNetworksMiner
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.junit.Test
import util.ProjectConfig
import util.UtilFunctions
import java.io.File
import kotlin.test.assertTrue

// TODO: implement
class CoEditNetworksMinerTests : GitMinerTest {
    @Test
    fun `test one thread and multithreading`() {
        runMiner(resourcesOneThreadDir)
//        runMiner(resourcesOneThreadDir, 1)
        runMiner(resourcesMultithreadingDir)

        val resultOneThread = replaceIds(loadCoEditNetwork(resourcesOneThreadDir), resourcesOneThreadDir)
        val resultMultithreading = replaceIds(loadCoEditNetwork(resourcesMultithreadingDir), resourcesMultithreadingDir)

        compare(resultOneThread, resultMultithreading)
    }

    private fun runMiner(resources: File, numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS) {
        val repository = FileRepository(File(repositoryDir, ".git"))
//        val repository = FileRepository(File("../test_repo_1/", ".git"))
        val miner = CoEditNetworksMiner(repository, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }

    private fun loadCoEditNetwork(resources: File): Set<CoEditNetworksMiner.CommitResult> {
        val file = File(resources, ProjectConfig.CO_EDIT)
        return Json.decodeFromString(file.readText())
    }

    private fun loadMappers(resources: File): Triple<HashMap<Int, String>, HashMap<Int, String>, HashMap<Int, String>> {
        val idToUser = Json.decodeFromString<HashMap<Int, String>>(File(resources, ProjectConfig.ID_USER).readText())
        val idToFile = Json.decodeFromString<HashMap<Int, String>>(File(resources, ProjectConfig.ID_FILE).readText())
        val idToCommit =
            Json.decodeFromString<HashMap<Int, String>>(File(resources, ProjectConfig.ID_COMMIT).readText())
        return Triple(idToUser, idToFile, idToCommit)
    }

    data class CommitResultWithoutId(
        val hash: String,
        val info: CommitInfoWithoutId,
        val edits: List<EditWithoutId>
    )

    data class CommitInfoWithoutId(
        val author: String,
        val date: Long
    )

    data class EditWithoutId(
        val oldPath: String,
        val newPath: String,
        val preStartLineNum: Int,
        val postStartLineNum: Int,
        val preLenInLines: Int,
        val postLenInLines: Int,
        val preLenInChars: Int,
        val postLenInChars: Int,
        val preEntropy: Double,
        val postEntropy: Double,
        val levenshtein: Int,
        val type: CoEditNetworksMiner.ChangeType
    )

    // TODO: better exceptions
    private fun replaceIds(set: Set<CoEditNetworksMiner.CommitResult>, resources: File): Set<CommitResultWithoutId> {
        val result = mutableSetOf<CommitResultWithoutId>()
        val (idToUserOne, idToFileOne, idToCommitOne) = loadMappers(resources)
        for (commitResult in set) {
            val commitId = commitResult.id
            val commitHash = idToCommitOne[commitId]!!

            val authorId = commitResult.info.author
            val author = idToUserOne[authorId]!!

            val edits = mutableListOf<EditWithoutId>()
            for (edit in commitResult.edits) {
                val oldPath = idToFileOne[edit.oldPath]!!
                val newPath = idToFileOne[edit.newPath]!!

                edits.add(
                    EditWithoutId(
                        oldPath,
                        newPath,
                        edit.preStartLineNum,
                        edit.postStartLineNum,
                        edit.preLenInLines,
                        edit.postLenInLines,
                        edit.preLenInChars,
                        edit.postLenInChars,
                        edit.preEntropy,
                        edit.postEntropy,
                        edit.levenshtein,
                        edit.type
                    )
                )
            }
            result.add(CommitResultWithoutId(commitHash, CommitInfoWithoutId(author, commitResult.info.date), edits))
        }

        return result
    }

    private fun compare(
        resultOneThread: Set<CommitResultWithoutId>,
        resultMultithreading: Set<CommitResultWithoutId>
    ) {
        assertTrue(
            resultOneThread.size == resultMultithreading.size &&
                    resultOneThread.containsAll(resultMultithreading) &&
                    resultMultithreading.containsAll(resultOneThread),
            "Not equal $resultOneThread != $resultMultithreading"
        )
    }
}