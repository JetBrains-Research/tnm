package miners.gitMiners

import GitMinerTest
import GitMinerTest.Companion.repository
import dataProcessor.FileDependencyMatrixDataProcessor
import org.junit.Test
import util.ProjectConfig
import kotlin.test.assertTrue

internal class FileDependencyMatrixMinerTests : GitMinerTest {

    @Test
    fun `test one thread and multithreading`() {
        val mapOneThread = runMiner(1)
        val mapMultithreading = runMiner()

        compareMapsOfMaps(mapOneThread, mapMultithreading)
    }

    private fun runMiner(numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS): Map<String, Map<String, Int>> {
        val dataProcessor = FileDependencyMatrixDataProcessor()

        val miner = FileDependencyMatrixMiner(repository, numThreads = numThreads)
        miner.run(dataProcessor)

        assertTrue(dataProcessor.fileDependencyMatrix.isNotEmpty())

        return changeIdsToValuesInMapOfMaps(
            dataProcessor.fileDependencyMatrix,
            dataProcessor.idToFile,
            dataProcessor.idToFile
        )
    }
}
