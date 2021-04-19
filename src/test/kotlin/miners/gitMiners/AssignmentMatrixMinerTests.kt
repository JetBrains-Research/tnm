package miners.gitMiners

import dataProcessor.AssignmentMatrixDataProcessor
import miners.gitMiners.GitMinerTest.Companion.branches
import miners.gitMiners.GitMinerTest.Companion.repository
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
        val miner = AssignmentMatrixMiner(repository, numThreads = numThreads, neededBranches = branches)
        miner.run(dataProcessor)

        assertTrue(dataProcessor.assignmentMatrix.isNotEmpty())

        return changeIdsToValuesInMapOfMaps(
            dataProcessor.assignmentMatrix,
            dataProcessor.idToUser,
            dataProcessor.idToFile
        )
    }
}
