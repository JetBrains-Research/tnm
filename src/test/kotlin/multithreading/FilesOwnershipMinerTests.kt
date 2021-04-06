package multithreading

import GitMinerNewTest
import GitMinerTest.Companion.repositoryDir
import dataProcessor.FilesOwnershipDataProcessor
import gitMiners.FilesOwnershipMiner
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.junit.Test
import util.ProjectConfig
import java.io.File
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class FilesOwnershipMinerTests : GitMinerNewTest {

    @Test
    fun `test one thread and multithreading`() {
        val mapOneThread = runMiner()
        val mapMultithreading = runMiner()
        compareMapsOfMapsDouble(mapOneThread, mapMultithreading)
    }

    private fun runMiner(
        numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
    ): Map<String, Map<String, Double>> {
        val dataProcessor = FilesOwnershipDataProcessor()
        val repository = FileRepository(File(repositoryDir, ".git"))
        val miner = FilesOwnershipMiner(repository, numThreads = numThreads)
        miner.run(dataProcessor)

        assertTrue(dataProcessor.developerKnowledge.isNotEmpty())

        return changeIdsToValuesInMapOfMaps(
            dataProcessor.developerKnowledge,
            dataProcessor.userMapper.idToUser,
            dataProcessor.fileMapper.idToFile
        )
    }
}
