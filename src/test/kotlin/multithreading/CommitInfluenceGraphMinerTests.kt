package multithreading

import GitMinerTest
import GitMinerTest.Companion.repository
import dataProcessor.CommitInfluenceGraphDataProcessor
import miners.gitMiners.CommitInfluenceGraphMiner
import org.junit.Test
import util.ProjectConfig
import kotlin.test.assertTrue

class CommitInfluenceGraphMinerTests : GitMinerTest {
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
            dataProcessor.idToCommit,
            dataProcessor.idToCommit
        )
    }

}