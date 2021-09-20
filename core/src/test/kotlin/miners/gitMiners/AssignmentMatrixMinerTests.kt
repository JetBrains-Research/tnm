package miners.gitMiners

import TestConfig.branches
import TestConfig.gitDir
import dataProcessor.AssignmentMatrixDataProcessor
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
        val miner = UserChangedFilesMiner(gitDir, numThreads = numThreads, neededBranches = branches)
        miner.run(dataProcessor)

        assertTrue(dataProcessor.assignmentMatrix.isNotEmpty())

        return changeIdsToValuesInMapOfMaps(
            dataProcessor.assignmentMatrix,
            dataProcessor.idToUser,
            dataProcessor.idToFile
        )
    }
}
