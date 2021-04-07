package multithreading

import GitMinerNewTest
import GitMinerNewTest.Companion.repository
import GitMinerTest
import GitMinerTest.Companion.repositoryDir
import GitMinerTest.Companion.resourcesMultithreadingDir
import GitMinerTest.Companion.resourcesOneThreadDir
import dataProcessor.CommitInfluenceGraphDataProcessor
import gitMiners.CommitInfluenceGraphMiner
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.junit.Test
import util.ProjectConfig
import java.io.File
import kotlin.test.assertTrue

class CommitInfluenceGraphMinerTests : GitMinerNewTest {
    @Test
    fun `test one thread and multithreading`() {
        val mapOneThread = runMiner(1)
        val mapMultithreading = runMiner()
        compareMapOfSets(mapOneThread, mapMultithreading)
    }

    private fun runMiner(numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS): Map<String, Set<String>> {
        val dataProcessor = CommitInfluenceGraphDataProcessor()
        val miner = CommitInfluenceGraphMiner(repository, numThreads = numThreads)
        miner.run(dataProcessor)

        assertTrue(dataProcessor.adjacencyMap.isNotEmpty())

        return changeIdsToValuesInMapOfSets(
            dataProcessor.adjacencyMap,
            dataProcessor.commitMapper.idToCommit,
            dataProcessor.commitMapper.idToCommit
        )
    }

}