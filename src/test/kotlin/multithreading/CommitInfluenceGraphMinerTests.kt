package multithreading

import GitMinerTest
import GitMinerTest.Companion.repositoryDir
import GitMinerTest.Companion.resourcesMultithreadingDir
import GitMinerTest.Companion.resourcesOneThreadDir
import gitMiners.CommitInfluenceGraphMiner
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.junit.Test
import util.ProjectConfig
import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CommitInfluenceGraphMinerTests : GitMinerTest {
    @Test
    fun `test one thread and multithreading`() {
        runMiner(resourcesOneThreadDir, 1)
        runMiner(resourcesMultithreadingDir)

        val mapOneThread = loadPageRank(resourcesOneThreadDir)
        val mapMultithreading = loadPageRank(resourcesMultithreadingDir)

        compare(mapOneThread, mapMultithreading)
    }

    private fun runMiner(resources: File, numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS) {
        val repository = FileRepository(File(repositoryDir, ".git"))
        val miner = CommitInfluenceGraphMiner(repository, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }

    private fun loadPageRank(resources: File): HashMap<String, MutableSet<String>> {
        val file = File(resources, ProjectConfig.COMMITS_GRAPH)
        val map = Json.decodeFromString<HashMap<Int, MutableSet<Int>>>(file.readText())
        val idToCommit = Json.decodeFromString<HashMap<Int, String>>(File(resources, "idToCommit").readText())

        val newMap = HashMap<String, MutableSet<String>>()
        for (entry in map.entries) {
            val commitId1 = entry.key
            for (commitId2 in entry.value) {
                val from = idToCommit[commitId1]
                assertNotNull(from, "can't find from $commitId1")

                val to = idToCommit[commitId2]
                assertNotNull(to, "can't find to $commitId2")

                newMap.computeIfAbsent(from) { mutableSetOf() }.add(to)
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
            )
        }
    }
}