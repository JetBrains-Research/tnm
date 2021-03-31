package multithreading

import GitMinerTest
import GitMinerTest.Companion.repositoryDir
import GitMinerTest.Companion.resourcesMultithreadingDir
import GitMinerTest.Companion.resourcesOneThreadDir
import gitMiners.ComplexityCodeChangesMiner
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.junit.Assert
import org.junit.Test
import util.ProjectConfig
import java.io.File

class ComplexityCodeChangesTests : GitMinerTest {
    @Test
    fun `test one thread and multithreading`() {
        runMiner(resourcesOneThreadDir, 1)
        runMiner(resourcesMultithreadingDir)

        val resultOneThread = loadResult(resourcesOneThreadDir)
        val resultMultithreading = loadResult(resourcesMultithreadingDir)
        compare(resultOneThread, resultMultithreading)
    }

    private fun loadResult(resources: File): HashMap<Int, ComplexityCodeChangesMiner.PeriodStats> {
        val file = File(resources, ProjectConfig.COMPLEXITY_CODE)
        return Json.decodeFromString(file.readText())
    }

    private fun runMiner(resources: File, numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS) {
        val repository = FileRepository(File(repositoryDir, ".git"))
        val miner = ComplexityCodeChangesMiner(repository, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }

    private fun compare(
        oneThreadResult: HashMap<Int, ComplexityCodeChangesMiner.PeriodStats>,
        multiThreadResult: HashMap<Int, ComplexityCodeChangesMiner.PeriodStats>
    ) {
        val idToFileOneThread =
            Json.decodeFromString<HashMap<Int, String>>(File(resourcesOneThreadDir, ProjectConfig.ID_FILE).readText())
        val fileToIdMultiThread =
            Json.decodeFromString<HashMap<String, Int>>(
                File(
                    resourcesMultithreadingDir,
                    ProjectConfig.FILE_ID
                ).readText()
            )

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