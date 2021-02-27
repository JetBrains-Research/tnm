package multithreading

import GitMinerTest
import GitMinerTest.Companion.repositoryDir
import GitMinerTest.Companion.resourcesMultithreadingDir
import GitMinerTest.Companion.resourcesOneThreadDir
import gitMiners.AssignmentMatrixMiner
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.junit.Test
import util.ProjectConfig
import java.io.File

internal class AssignmentMatrixMinerTests : GitMinerTest {

    @Test
    fun `test one thread and multithreading`() {
        runMiner(resourcesOneThreadDir, 1)
        runMiner(resourcesMultithreadingDir)

        val mapOneThread = loadAssignmentMatrix(resourcesOneThreadDir)
        val mapMultithreading = loadAssignmentMatrix(resourcesMultithreadingDir)

        compareMapsOfMaps(mapOneThread, mapMultithreading)
    }

    private fun runMiner(resources: File, numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS) {
        val repository = FileRepository(File(repositoryDir, ".git"))
        val miner = AssignmentMatrixMiner(repository, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }

    private fun loadAssignmentMatrix(resources: File): HashMap<String, HashMap<String, Int>> {
        val file = File(resources, ProjectConfig.ASSIGNMENT_MATRIX)
        val map = Json.decodeFromString<HashMap<Int, HashMap<Int, Int>>>(file.readText())
        val idToUser = Json.decodeFromString<HashMap<Int, String>>(File(resources, ProjectConfig.ID_USER).readText())
        val idToFile = Json.decodeFromString<HashMap<Int, String>>(File(resources, ProjectConfig.ID_FILE).readText())

        return changeIdsToValuesInMapOfMaps(map, idToUser, idToFile)
    }
}
