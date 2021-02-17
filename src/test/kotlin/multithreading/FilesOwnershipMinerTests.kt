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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class FilesOwnershipMinerTests : GitMinerTest {

    @Test
    fun `test one thread and multithreading`() {
        runMiner(resourcesOneThreadDir, 1)
        runMiner(resourcesMultithreadingDir)

        val mapOneThread = load(resourcesOneThreadDir)
        val mapMultithreading = load(resourcesMultithreadingDir)

        compare(mapOneThread, mapMultithreading)
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

        return changeIdsToValuesInMap(map, idToUser, idToFile)
    }

    private fun <T> changeIdsToValuesInMap(
        map: HashMap<Int, HashMap<Int, T>>,
        keys1: HashMap<Int, String>,
        keys2: HashMap<Int, String>
    ): HashMap<String, HashMap<String, T>> {
        val newMap = HashMap<String, HashMap<String, T>>()
        for (entry1 in map) {
            for (entry2 in entry1.value) {
                val key1 = keys1[entry1.key]
                val key2 = keys2[entry2.key]
                assertNotNull(key1, "can't find key1 ${entry1.key}")
                assertNotNull(key2, "can't find key2 ${entry2.key}")
                newMap
                    .computeIfAbsent(key1) { HashMap() }
                    .computeIfAbsent(key2) { entry2.value }
            }
        }

        return newMap
    }

    private fun compare(
        mapOneThread: HashMap<String, HashMap<String, Double>>,
        mapMultithreading: HashMap<String, HashMap<String, Double>>
    ) {

        for (entry1 in mapOneThread.entries) {
            for (entry2 in entry1.value.entries) {
                val k1 = entry1.key
                val k2 = entry2.key

                val v1 = mapOneThread[k1]?.get(k2)
                assertNotNull(v1, "got null in v1 : [$k1][$k2]")

                val v2 = mapMultithreading[k1]?.get(k2)
                assertNotNull(v2, "got null in v2 : [$k1][$k2]")
                assertEquals(v1, v2, "Found non equal values in [$k1][$k2]: $v1 != $v2")
            }
        }
    }

}
