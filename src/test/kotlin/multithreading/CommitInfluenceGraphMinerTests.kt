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

class CommitInfluenceGraphMinerTests : GitMinerTest {
    @Test
    fun `test one thread and multithreading`() {
        runMiner(resourcesOneThreadDir, 1)
        runMiner(resourcesMultithreadingDir)

        val mapOneThread = loadPageRank(resourcesOneThreadDir)
        val mapMultithreading = loadPageRank(resourcesMultithreadingDir)

        compareMapOfSets(mapOneThread, mapMultithreading)
    }

    private fun runMiner(resources: File, numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS) {
        val repository = FileRepository(File(repositoryDir, ".git"))
        val miner = CommitInfluenceGraphMiner(repository, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }

    private fun loadPageRank(resources: File): HashMap<String, Set<String>> {
        val file = File(resources, ProjectConfig.COMMITS_GRAPH)
        val map = Json.decodeFromString<HashMap<Int, Set<Int>>>(file.readText())
        val idToCommit = Json.decodeFromString<HashMap<Int, String>>(File(resources, "idToCommit").readText())

        return changeIdsToValuesInMapOfSets(map, idToCommit, idToCommit)
    }
}