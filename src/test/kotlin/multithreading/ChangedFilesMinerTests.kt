package multithreading

import GitMinerTest
import GitMinerTest.Companion.repositoryDir
import GitMinerTest.Companion.resourcesMultithreadingDir
import GitMinerTest.Companion.resourcesOneThreadDir
import gitMiners.ChangedFilesMiner
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.junit.Test
import util.ProjectConfig
import java.io.File

internal class ChangedFilesMinerTests : GitMinerTest {
    @Test
    fun `test one thread and multithreading`() {
        runMiner(resourcesOneThreadDir, 1)
        runMiner(resourcesMultithreadingDir)

        val mapOneThread = loadChangedFiles(resourcesOneThreadDir)
        val mapMultithreading = loadChangedFiles(resourcesMultithreadingDir)

        compareMapOfSets(mapOneThread, mapMultithreading)

    }

    private fun runMiner(resources: File, numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS) {
        val repository = FileRepository(File(repositoryDir, ".git"))
        val miner = ChangedFilesMiner(repository, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }

    private fun loadChangedFiles(resources: File): HashMap<String, Set<String>> {
        val file = File(resources, ProjectConfig.USER_FILES_IDS)
        val map = Json.decodeFromString<HashMap<Int, Set<Int>>>(file.readText())
        val idToFile = Json.decodeFromString<HashMap<Int, String>>(File(resources, ProjectConfig.ID_FILE).readText())
        val idToUser = Json.decodeFromString<HashMap<Int, String>>(File(resources, ProjectConfig.ID_USER).readText())

        return changeIdsToValuesInMapOfSets(map, idToUser, idToFile)
    }

}