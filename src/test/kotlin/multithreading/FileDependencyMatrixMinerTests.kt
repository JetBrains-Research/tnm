package multithreading

import GitMinerTest
import GitMinerTest.Companion.repositoryDir
import GitMinerTest.Companion.resourcesMultithreadingDir
import GitMinerTest.Companion.resourcesOneThreadDir
import gitMiners.FileDependencyMatrixMiner
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.junit.Test
import util.ProjectConfig
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class FileDependencyMatrixMinerTests : GitMinerTest {

    @Test
    fun `test one thread and multithreading`() {
        runMiner(resourcesOneThreadDir, 1)
        runMiner(resourcesMultithreadingDir)

        val mapOneThread = loadFileDependency(resourcesOneThreadDir)
        val mapMultithreading = loadFileDependency(resourcesMultithreadingDir)

        compare(mapOneThread, mapMultithreading)
    }

    private fun runMiner(resources: File, numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS) {
        val repository = FileRepository(File(repositoryDir, ".git"))
        val miner = FileDependencyMatrixMiner(repository, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }

    private fun loadFileDependency(resources: File): HashMap<String, HashMap<String, Int>> {
        val file = File(resources, ProjectConfig.FILE_DEPENDENCY)
        val map = Json.decodeFromString<HashMap<Int, HashMap<Int, Int>>>(file.readText())
        val idToFile = Json.decodeFromString<HashMap<Int, String>>(File(resources, ProjectConfig.ID_FILE).readText())

        val newMap = HashMap<String, HashMap<String, Int>>()
        for (entry1 in map.entries) {
            for (entry2 in entry1.value.entries) {
                val fileId1 = entry1.key
                val filePath1 = idToFile[fileId1]
                assertNotNull(filePath1, "can't find file $filePath1 with id $fileId1")

                val fileId2 = entry2.key
                val filePath2 = idToFile[fileId2]
                assertNotNull(filePath2, "can't find file $filePath2 with id $fileId2")

                val value = entry2.value
                newMap.computeIfAbsent(filePath1) { HashMap() }
                    .computeIfAbsent(filePath2) { value }
            }
        }
        return newMap
    }

    private fun compare(
        mapOneThread: HashMap<String, HashMap<String, Int>>,
        mapMultithreading: HashMap<String, HashMap<String, Int>>
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