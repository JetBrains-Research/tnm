package multithreading

import GitMinerTest
import GitMinerTest.Companion.repositoryDir
import GitMinerTest.Companion.resourcesMultithreadingDir
import GitMinerTest.Companion.resourcesOneThreadDir
import gitMiners.WorkTimeMiner
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.junit.Test
import util.ProjectConfig
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class WorkTimeMinerTests : GitMinerTest {
    @Test
    fun `test one thread and multithreading`() {
        runMiner(resourcesOneThreadDir, 1)
        runMiner(resourcesMultithreadingDir)

        val mapOneThread = loadWorkTime(resourcesOneThreadDir)
        val mapMultithreading = loadWorkTime(resourcesMultithreadingDir)

        compare(mapOneThread, mapMultithreading)
    }

    private fun runMiner(resources: File, numThreads: Int = ProjectConfig.numThreads) {
        val repository = FileRepository(File(repositoryDir, ".git"))
        val miner = WorkTimeMiner(repository, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }

    private fun loadWorkTime(resources: File): HashMap<String, HashMap<Int, Int>> {
        val file = File(resources, ProjectConfig.WORKTIME_DISTRIBUTION)
        val map = Json.decodeFromString<HashMap<Int, HashMap<Int, Int>>>(file.readText())
        val idToUser = Json.decodeFromString<HashMap<Int, String>>(File(resources, ProjectConfig.ID_USER).readText())

        val newMap = HashMap<String, HashMap<Int, Int>>()
        for (entry1 in map.entries) {
            val userId = entry1.key
            val user = idToUser[userId]
            assertNotNull(user, "can't find user $userId")

            for (entry2 in entry1.value.entries) {
                newMap.computeIfAbsent(user) { HashMap() }
                    .computeIfAbsent(entry2.key) { entry2.value }
            }
        }
        return newMap
    }

    private fun compare(
        mapOneThread: HashMap<String, HashMap<Int, Int>>,
        mapMultithreading: HashMap<String, HashMap<Int, Int>>
    ) {
        for (entry1 in mapOneThread.entries) {
            for (entry2 in entry1.value.entries) {
                val k1 = entry1.key
                val k2 = entry2.key

                val v1 = mapOneThread[k1]?.get(k2)
                assertNotNull(v1, "got null in v1 : [$k1][$k2]")

                val v2 = mapMultithreading[k1]?.get(k2)
                assertNotNull(v2, "got null in v2 : [$k1][$k2]")

                if (v1 != v2) {
                    assertEquals(v1, v2, "Found non equal values in [$k1][$k2]: $v1 != $v2")
                }
            }
        }
    }
}
