package miners.gitMiners

import TestConfig.branch
import TestConfig.gitDir
import dataProcessor.FilesOwnershipDataProcessor
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.test.assertTrue

internal class FilesOwnershipMinerTests : GitMinerTest<Map<String, Map<String, Float>>>() {

    override val serializer = MapSerializer(String.serializer(), MapSerializer(String.serializer(), Float.serializer()))

    override fun runMiner(
        numThreads: Int
    ): Map<String, Map<String, Float>> {
        val dataProcessor = FilesOwnershipDataProcessor()
        val miner = FilesOwnershipMiner(gitDir, numThreads = numThreads, neededBranch = branch)
        miner.run(dataProcessor)

        assertTrue(dataProcessor.developerKnowledge.isNotEmpty())

        return changeIdsToValuesInMapOfMaps(
            dataProcessor.developerKnowledge,
            dataProcessor.idToUser,
            dataProcessor.idToFile
        )
    }

    override fun compareResults(result1: Map<String, Map<String, Float>>, result2: Map<String, Map<String, Float>>) =
        compareMapsOfMapsDouble(result1, result2)

}
