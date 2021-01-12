package multithreading

import GitMinerTest
import GitMinerTest.Companion.repositoryDir
import GitMinerTest.Companion.resourcesMultithreadingDir
import GitMinerTest.Companion.resourcesOneThreadDir
import gitMiners.AssignmentMatrixMiner
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.junit.Test
import util.ProjectConfig
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class AssignmentMatrixMinerTests : GitMinerTest {

    @Test
    fun `test one thread and multithreading`() {
        runMiner(resourcesOneThreadDir, 1)
        runMiner(resourcesMultithreadingDir)

        val mapOneThread = loadAssignmentMatrix(resourcesOneThreadDir)
        val mapMultithreading = loadAssignmentMatrix(resourcesMultithreadingDir)

        compare(mapOneThread, mapMultithreading)
    }

    private fun runMiner(resources: File, numThreads: Int = ProjectConfig.numThreads) {
        val repository = FileRepository(File(repositoryDir, ".git"))
        val miner = AssignmentMatrixMiner(repository, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }

    private fun loadAssignmentMatrix(resources: File): HashMap<String, HashMap<String, Int>> {
        val file = File(resources, ProjectConfig.ASSIGNMENT_MATRIX)
        val map = Json.decodeFromString<HashMap<Int, HashMap<Int, Int>>>(file.readText())
        val idToUser = Json.decodeFromString<HashMap<Int, String>>(File(resources, ProjectConfig.ID_USER).readText())
        val idToFile = Json.decodeFromString<HashMap<Int, String>>(File(resources, ProjectConfig.ID_FILE).readText())

        val newMap = HashMap<String, HashMap<String, Int>>()
        for (entry in map.entries) {
            for (e in entry.value.entries) {
                val userMail = idToUser[entry.key]
                val filePath = idToFile[e.key]
                assertNotNull(userMail, "can't find user ${entry.key}")
                assertNotNull(filePath, "can't find file ${e.key}")
                newMap.computeIfAbsent(userMail) { HashMap() }
                    .computeIfAbsent(filePath) { e.value }
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
