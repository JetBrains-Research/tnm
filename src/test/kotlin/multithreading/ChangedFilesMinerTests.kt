package multithreading

import GitMinerNewTest
import GitMinerNewTest.Companion.repository
import dataProcessor.ChangedFilesDataProcessor
import gitMiners.ChangedFilesMiner
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Test
import util.ProjectConfig
import java.io.File
import kotlin.test.assertTrue

internal class ChangedFilesMinerTests : GitMinerNewTest {
    @Test
    fun `test one thread and multithreading`() {
        val mapOneThread = runMiner(1)
        val mapMultithreading = runMiner()

        compareMapOfSets(mapOneThread, mapMultithreading)
    }

    private fun runMiner(numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS): Map<String, Set<String>> {
        val dataProcessor = ChangedFilesDataProcessor()
        val miner = ChangedFilesMiner(repository, numThreads = numThreads)
        miner.run(dataProcessor)

        assertTrue(dataProcessor.userFilesIds.isNotEmpty())

        return changeIdsToValuesInMapOfSets(
            dataProcessor.userFilesIds,
            dataProcessor.userMapper.idToUser,
            dataProcessor.fileMapper.idToFile
        )
    }
}