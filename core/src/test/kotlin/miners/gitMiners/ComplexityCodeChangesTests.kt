package miners.gitMiners

import TestConfig.branch
import TestConfig.gitDir
import dataProcessor.ComplexityCodeChangesDataProcessor
import kotlinx.serialization.Serializable
import org.junit.Assert
import kotlin.test.assertTrue

class ComplexityCodeChangesTests : GitMinerTest<ComplexityCodeChangesTests.Stats>() {

    @Serializable
    data class Stats(
        val periodsToStats: Map<Int, ComplexityCodeChangesDataProcessor.PeriodStats>,
        val idToFile: Map<Int, String>,
        val fileToId: Map<String, Int>
    )

    override val serializer = Stats.serializer()

    override fun runMiner(numThreads: Int): Stats {
        val dataProcessor =
            ComplexityCodeChangesDataProcessor()
        val miner = ComplexityCodeChangesMiner(gitDir, neededBranch = branch, numThreads = numThreads)
        miner.run(dataProcessor)

        assertTrue(dataProcessor.periodsToStats.isNotEmpty())

        return Stats(dataProcessor.periodsToStats, dataProcessor.idToFile, dataProcessor.fileToId)
    }

    override fun compareResults(
        result1: Stats,
        result2: Stats
    ) {
        val idToFile1 = result1.idToFile
        val fileToId2 = result2.fileToId

        val periodToStats1 = result1.periodsToStats
        val periodToStats2 = result2.periodsToStats

        for ((periodId, stats1) in periodToStats1) {
            val stats2 = periodToStats2[periodId]!!
            Assert.assertEquals(stats1.periodEntropy, stats2.periodEntropy, 0.0001)

            val fileStatsMultiThread = stats2.filesStats
            for ((fileId1, fileStats1) in stats1.filesStats) {
                val fileName = idToFile1[fileId1]!!
                val fileId2 = fileToId2[fileName]!!
                val fileStat2 = fileStatsMultiThread[fileId2]!!

                Assert.assertEquals(fileStats1.entropy, fileStat2.entropy, 0.0001)
                Assert.assertEquals(
                    fileStats1.HCPF3,
                    fileStat2.HCPF3,
                    0.0001
                )
                Assert.assertEquals(
                    fileStats1.HCPF2,
                    fileStat2.HCPF2,
                    0.0001
                )
            }
        }
    }

}