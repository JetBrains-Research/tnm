package miners.gitMiners

import TestConfig.branches
import TestConfig.repository
import dataProcessor.WorkTimeDataProcessor
import org.junit.Test
import util.ProjectConfig
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class WorkTimeMinerTests : GitMinerTest {
    @Test
    fun `test one thread and multithreading`() {
        val mapOneThread = runMiner(1)
        val mapMultithreading = runMiner()

        compare(mapOneThread, mapMultithreading)
    }

    private fun runMiner(numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS): Map<String, Map<Int, Int>> {
        val dataProcessor = WorkTimeDataProcessor()
        val miner = WorkTimeMiner(repository, numThreads = numThreads, neededBranches = branches)
        miner.run(dataProcessor)

        val newMap = HashMap<String, HashMap<Int, Int>>()
        for (entry1 in dataProcessor.workTimeDistribution.entries) {
            val userId = entry1.key
            val user = dataProcessor.idToUser[userId]
            assertNotNull(user, "can't find user $userId")

            for (entry2 in entry1.value.entries) {
                newMap.computeIfAbsent(user) { HashMap() }
                    .computeIfAbsent(entry2.key) { entry2.value }
            }
        }
        return newMap

    }

    private fun compare(
        mapOneThread: Map<String, Map<Int, Int>>,
        mapMultithreading: Map<String, Map<Int, Int>>
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
