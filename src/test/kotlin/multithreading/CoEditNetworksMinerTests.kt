package multithreading

import GitMinerTest
import GitMinerTest.Companion.repositoryDir
import GitMinerTest.Companion.resourcesMultithreadingDir
import GitMinerTest.Companion.resourcesOneThreadDir
import gitMiners.CoEditNetworksMiner
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.junit.Test
import util.ProjectConfig
import java.io.File
import kotlin.test.assertNotNull

// TODO: implement
class CoEditNetworksMinerTests : GitMinerTest {
//    @Test
    fun `test one thread and multithreading`() {
        runMiner(resourcesOneThreadDir, 1)
        runMiner(resourcesMultithreadingDir)

        val resultOneThread = loadCoEditNetwork(resourcesOneThreadDir)
        val resultMultithreading = loadCoEditNetwork(resourcesMultithreadingDir)

        compare(resultOneThread, resultMultithreading)
    }

    private fun runMiner(resources: File, numThreads: Int = ProjectConfig.numThreads) {
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
        val idToCommit = Json.decodeFromString<HashMap<Int, String>>(File(resources, ProjectConfig.ID_COMMIT).readText())
        return Triple(idToUser, idToFile, idToCommit)
    }

    private fun compare(
        resultOneThread: Set<CoEditNetworksMiner.CommitResult>,
        resultMultithreading: Set<CoEditNetworksMiner.CommitResult>
    ) {
        val (idToUserOne, idToFileOne, idToCommitOne) = loadMappers(resourcesOneThreadDir)
        val (idToUserMulti, idToFileMulti, idToCommitMulti) = loadMappers(resourcesMultithreadingDir)

        for (valueOne in resultOneThread) {
            val commitHash = idToCommitOne[valueOne.id]
            assertNotNull(commitHash, "got null commitHash in oneThread : [${valueOne.id}]")

            val valueMulti = resultMultithreading.find {
                val commitHashMulti = idToCommitMulti[it.id]
                assertNotNull(commitHashMulti, "got null commitHash in multiThread : [${it.id}]")
                commitHash == commitHashMulti
            }


        }


    }
}