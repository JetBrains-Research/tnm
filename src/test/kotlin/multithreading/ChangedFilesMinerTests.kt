package multithreading

import GitMinerTest
import GitMinerTest.Companion.repositoryDir
import GitMinerTest.Companion.resourcesMultithreadingDir
import GitMinerTest.Companion.resourcesOneThreadDir
import gitMiners.ChangedFilesMiner
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.junit.Test
import util.ProjectConfig
import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class ChangedFilesMinerTests : GitMinerTest {
    @Test
    fun `test one thread and multithreading`() {
        runMiner(resourcesOneThreadDir, 1)
        runMiner(resourcesMultithreadingDir)

        val mapOneThread = loadChangedFiles(resourcesOneThreadDir)
        val mapMultithreading = loadChangedFiles(resourcesMultithreadingDir)

        compare(mapOneThread, mapMultithreading)

    }

    private fun runMiner(resources: File, numThreads: Int = ProjectConfig.numThreads) {
        val repository = FileRepository(File(repositoryDir, ".git"))
        val miner = ChangedFilesMiner(repository, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }

    private fun loadChangedFiles(resources: File): HashMap<String, MutableSet<String>> {
        val file = File(resources, ProjectConfig.USER_FILES_IDS)
        val map = Json.decodeFromString<HashMap<Int, MutableSet<Int>>>(file.readText())
        val idToFile = Json.decodeFromString<HashMap<Int, String>>(File(resources, ProjectConfig.ID_FILE).readText())
        val idToUser = Json.decodeFromString<HashMap<Int, String>>(File(resources, ProjectConfig.ID_USER).readText())

        val newMap = HashMap<String, MutableSet<String>>()
        for (entry in map.entries) {
            val userId = entry.key
            for (fileId in entry.value) {
                val user = idToUser[userId]
                val fileName = idToFile[fileId]

                assertNotNull(user, "can't find user $userId")
                assertNotNull(fileName, "can't find file $fileId")

                newMap.computeIfAbsent(user) { mutableSetOf() }.add(fileName)
            }
        }
        return newMap
    }

    private fun compare(
        mapOneThread: HashMap<String, MutableSet<String>>,
        mapMultithreading: HashMap<String, MutableSet<String>>
    ) {
        for (entry in mapOneThread.entries) {
            val userName = entry.key
            val v1 = entry.value
            val v2 = mapMultithreading[userName]
            assertNotNull(v2, "got null in v2 for user $userName")
            assertTrue(
                v1.size == v2.size && v1.containsAll(v2) && v2.containsAll(v1),
                "Not equal $v1 != $v2"
            );
        }
    }
}