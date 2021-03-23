package multithreading

import GitMinerTest
import GitMinerTest.Companion.repositoryDir
import GitMinerTest.Companion.resourcesMultithreadingDir
import GitMinerTest.Companion.resourcesOneThreadDir
import gitMiners.FilesOwnershipMiner
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.junit.Test
import util.ProjectConfig
import java.io.File

internal class FilesOwnershipMinerTests : GitMinerTest {

    @Test
    fun `test one thread and multithreading`() {
        runMiner(resourcesOneThreadDir, 1)
        runMiner(resourcesMultithreadingDir)

        val mapOneThread = load(resourcesOneThreadDir)
        val mapMultithreading = load(resourcesMultithreadingDir)

        compareMapsOfMapsDouble(mapOneThread, mapMultithreading)
    }

    private fun runMiner(resources: File, numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS) {
        val repository = FileRepository(File(repositoryDir, ".git"))
        val miner = FilesOwnershipMiner(repository, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }

    private fun load(resources: File): HashMap<String, HashMap<String, Double>> {
        val file = File(resources, ProjectConfig.DEVELOPER_KNOWLEDGE)
        val map = Json.decodeFromString<HashMap<Int, HashMap<Int, Double>>>(file.readText())

        val idToUser = Json.decodeFromString<HashMap<Int, String>>(File(resources, ProjectConfig.ID_USER).readText())
        val idToFile = Json.decodeFromString<HashMap<Int, String>>(File(resources, ProjectConfig.ID_FILE).readText())

        return changeIdsToValuesInMapOfMaps(map, idToUser, idToFile)
    }
}
