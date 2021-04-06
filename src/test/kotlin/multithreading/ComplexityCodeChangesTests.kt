package multithreading

import GitMinerNewTest
import GitMinerTest
import GitMinerTest.Companion.repositoryDir
import GitMinerTest.Companion.resourcesMultithreadingDir
import GitMinerTest.Companion.resourcesOneThreadDir
import dataProcessor.ComplexityCodeChangesDataProcessor
import dataProcessor.ComplexityCodeChangesDataProcessor.PeriodStats
import gitMiners.ComplexityCodeChangesMiner
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.junit.Assert
import org.junit.Test
import util.ProjectConfig
import java.io.File
import kotlin.test.assertTrue

class ComplexityCodeChangesTests : GitMinerNewTest {
    @Test
    fun `test one thread and multithreading`() {
        val dataProcessorOneThread = runMiner(1)
        val dataProcessorMultithreading = runMiner()

        compare(dataProcessorOneThread, dataProcessorMultithreading)
    }

    private fun runMiner(numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS): ComplexityCodeChangesDataProcessor {
        val dataProcessor = ComplexityCodeChangesDataProcessor()
        val repository = FileRepository(File(repositoryDir, ".git"))
        val miner = ComplexityCodeChangesMiner(repository, numThreads = numThreads)
        miner.run(dataProcessor)

        assertTrue(dataProcessor.periodsToStats.isNotEmpty())

        return dataProcessor

    }

    private fun compare(
        dataProcessorOneThread: ComplexityCodeChangesDataProcessor,
        dataProcessorMultithreading: ComplexityCodeChangesDataProcessor
    ) {
        val idToFileOneThread = dataProcessorOneThread.fileMapper.idToFile
        val fileToIdMultiThread = dataProcessorMultithreading.fileMapper.fileToId

        val oneThreadResult = dataProcessorOneThread.periodsToStats
        val multiThreadResult = dataProcessorMultithreading.periodsToStats

        for ((periodId, statsOneThread) in oneThreadResult) {
            val statsMultiThread = multiThreadResult[periodId]!!
            Assert.assertEquals(statsOneThread.periodEntropy, statsMultiThread.periodEntropy, 0.0001)

            val fileStatsMultiThread = statsMultiThread.filesStats
            for ((fileIdOneThread, fileStatOneThread) in statsOneThread.filesStats) {
                val fileName = idToFileOneThread[fileIdOneThread]!!
                val fileIdMultiThread = fileToIdMultiThread[fileName]!!
                val fileStatMultiThread = fileStatsMultiThread[fileIdMultiThread]!!

                Assert.assertEquals(fileStatOneThread.entropy, fileStatMultiThread.entropy, 0.0001)
                Assert.assertEquals(
                    fileStatOneThread.HCPF3,
                    fileStatMultiThread.HCPF3,
                    0.0001
                )
                Assert.assertEquals(
                    fileStatOneThread.HCPF2,
                    fileStatMultiThread.HCPF2,
                    0.0001
                )
            }
        }
    }

}