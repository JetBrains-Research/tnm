package multithreading

import GitMinerNewTest
import GitMinerNewTest.Companion.repository
import dataProcessor.FileDependencyMatrixDataProcessor
import gitMiners.FileDependencyMatrixMiner
import org.junit.Test
import util.ProjectConfig
import kotlin.test.assertTrue

internal class FileDependencyMatrixMinerTests : GitMinerNewTest {

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
            dataProcessor.fileMapper.idToFile,
            dataProcessor.fileMapper.idToFile
        )
    }
}
