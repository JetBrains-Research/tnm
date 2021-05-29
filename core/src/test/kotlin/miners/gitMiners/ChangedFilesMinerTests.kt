package miners.gitMiners

import TestConfig.branches
import TestConfig.gitDir
import dataProcessor.ChangedFilesDataProcessor
import org.junit.Test
import util.ProjectConfig
import kotlin.test.assertTrue

internal class ChangedFilesMinerTests : GitMinerTest {
    @Test
    fun `test one thread and multithreading`() {
        val mapOneThread = runMiner(1)
        val mapMultithreading = runMiner()

        compareMapOfSets(mapOneThread, mapMultithreading)
    }

    private fun runMiner(numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS): Map<String, Set<String>> {
        val dataProcessor = ChangedFilesDataProcessor()
        val miner = ChangedFilesMiner(gitDir, numThreads = numThreads, neededBranches = branches)
        miner.run(dataProcessor)

        assertTrue(dataProcessor.changedFilesByUsers.isNotEmpty())

        return changeIdsToValuesInMapOfSets(
            dataProcessor.changedFilesByUsers,
            dataProcessor.idToUser,
            dataProcessor.idToFile
        )
    }
}