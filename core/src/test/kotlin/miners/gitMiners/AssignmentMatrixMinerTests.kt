package miners.gitMiners

import TestConfig.branches
import TestConfig.gitDir
import dataProcessor.AssignmentMatrixDataProcessor
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.test.assertTrue

internal class AssignmentMatrixMinerTests : GitMinerTest<Map<String, Map<String, Int>>>() {

    override val serializer = MapSerializer(String.serializer(), MapSerializer(String.serializer(), Int.serializer()))

    override fun compareResults(result1: Map<String, Map<String, Int>>, result2: Map<String, Map<String, Int>>) =
        compareMapsOfMaps(result1, result2)


    override fun runMiner(numThreads: Int): Map<String, Map<String, Int>> {
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
