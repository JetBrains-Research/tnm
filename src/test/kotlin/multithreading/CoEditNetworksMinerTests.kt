package multithreading

import GitMinerTest
import GitMinerTest.Companion.repositoryDir
import GitMinerTest.Companion.resourcesMultithreadingDir
import GitMinerTest.Companion.resourcesOneThreadDir
import gitMiners.CoEditNetworksMiner
import gitMiners.CoEditNetworksMiner.Companion.EMPTY_VALUE
import gitMiners.CoEditNetworksMiner.Companion.EMPTY_VALUE_ID
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.junit.Test
import util.ProjectConfig
import java.io.File

class CoEditNetworksMinerTests : GitMinerTest {
    @Test
    fun `test one thread and multithreading`() {
        runMiner(resourcesOneThreadDir, 1)
        runMiner(resourcesMultithreadingDir)

        val resultOneThread = replaceIds(loadCoEditNetwork(resourcesOneThreadDir), resourcesOneThreadDir)
        val resultMultithreading = replaceIds(loadCoEditNetwork(resourcesMultithreadingDir), resourcesMultithreadingDir)

        compareSets(resultOneThread, resultMultithreading)
    }

    private fun runMiner(resources: File, numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS) {
        val repository = FileRepository(File(repositoryDir, ".git"))
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

        idToUser[EMPTY_VALUE_ID] = EMPTY_VALUE
        idToFile[EMPTY_VALUE_ID] = EMPTY_VALUE
        idToCommit[EMPTY_VALUE_ID] = EMPTY_VALUE

        return Triple(idToUser, idToFile, idToCommit)
    }

    data class CommitResultWithoutId(
        val prevCommitInfo: CommitInfoWithoutId,
        val commitInfo: CommitInfoWithoutId,
        val nextCommitInfo: CommitInfoWithoutId,
        val edits: List<EditWithoutId>
    )

    data class CommitInfoWithoutId(
        val hash: String,
        val author: String,
        val date: Long
    ) {
        constructor(
            commitInfo: CoEditNetworksMiner.CommitInfo,
            idToCommit: HashMap<Int, String>,
            idToUser: HashMap<Int, String>
        ) :
                this(
                    idToCommit[commitInfo.id]!!,
                    idToUser[commitInfo.author]!!,
                    commitInfo.date
                )
    }

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
        val (idToUser, idToFile, idToCommit) = loadMappers(resources)

        for (commitResult in set) {

            val edits = mutableListOf<EditWithoutId>()
            for (edit in commitResult.edits) {
                val oldPath = idToFile[edit.oldPath]!!
                val newPath = idToFile[edit.newPath]!!

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

            result.add(
                CommitResultWithoutId(
                    CommitInfoWithoutId(commitResult.prevCommitInfo, idToCommit, idToUser),
                    CommitInfoWithoutId(commitResult.commitInfo, idToCommit, idToUser),
                    CommitInfoWithoutId(commitResult.nextCommitInfo, idToCommit, idToUser),
                    edits
                )
            )

        }

        return result
    }
}
