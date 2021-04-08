package multithreading

import GitMinerTest
import GitMinerTest.Companion.repository
import dataProcessor.AssignmentMatrixDataProcessor
import gitMiners.AssignmentMatrixMiner
import org.junit.Test
import util.ProjectConfig
import kotlin.test.assertTrue

internal class AssignmentMatrixMinerTests : GitMinerTest {

    @Test
    fun `test one thread and multithreading`() {
        val mapOneThread = runMiner(1)
        val mapMultithreading = runMiner()

        compareMapsOfMaps(mapOneThread, mapMultithreading)
    }

    private fun runMiner(numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS): Map<String, Map<String, Int>> {
        val dataProcessor = AssignmentMatrixDataProcessor()
        val miner = AssignmentMatrixMiner(repository, numThreads = numThreads)
        miner.run(dataProcessor)

        assertTrue(dataProcessor.assignmentMatrix.isNotEmpty())

        return changeIdsToValuesInMapOfMaps(
            dataProcessor.assignmentMatrix,
            dataProcessor.idToUser,
            dataProcessor.idToFile
        )
    }
}
