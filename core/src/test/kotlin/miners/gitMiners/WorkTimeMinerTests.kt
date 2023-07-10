package miners.gitMiners

import TestConfig.branches
import TestConfig.gitDir
import dataProcessor.WorkTimeDataProcessor
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class WorkTimeMinerTests : GitMinerTest<Map<String, Map<Int, Int>>>() {

    override val serializer = MapSerializer(String.serializer(), MapSerializer(Int.serializer(), Int.serializer()))

    override fun runMiner(numThreads: Int): Map<String, Map<Int, Int>> {
        val dataProcessor = WorkTimeDataProcessor()
        val miner = WorkTimeMiner(gitDir, numThreads = numThreads, neededBranches = branches)
        miner.run(dataProcessor)

        assertTrue(dataProcessor.workTimeDistribution.isNotEmpty())

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

    override fun compareResults(result1: Map<String, Map<Int, Int>>, result2: Map<String, Map<Int, Int>>) {
        for (entry1 in result1.entries) {
            for (entry2 in entry1.value.entries) {
                val k1 = entry1.key
                val k2 = entry2.key

                val v1 = result1[k1]?.get(k2)
                assertNotNull(v1, "got null in v1 : [$k1][$k2]")

                val v2 = result2[k1]?.get(k2)
                assertNotNull(v2, "got null in v2 : [$k1][$k2]")

                if (v1 != v2) {
                    assertEquals(v1, v2, "Found non equal values in [$k1][$k2]: $v1 != $v2")
                }
            }
        }
    }

}
