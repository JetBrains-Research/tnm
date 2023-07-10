package miners.gitMiners

import TestConfig.branches
import TestConfig.gitDir
import dataProcessor.CommitInfluenceGraphDataProcessor
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.test.assertTrue

class CommitInfluenceGraphMinerTests : GitMinerTest<Map<String, Set<String>>>() {

    override val serializer = MapSerializer(String.serializer(), SetSerializer(String.serializer()))

    override fun runMiner(numThreads: Int): Map<String, Set<String>> {
        val dataProcessor = CommitInfluenceGraphDataProcessor()
        val miner = CommitInfluenceGraphMiner(gitDir, numThreads = numThreads, neededBranches = branches)
        miner.run(dataProcessor)

        assertTrue(dataProcessor.adjacencyMap.isNotEmpty())

        return changeIdsToValuesInMapOfSets(
            dataProcessor.adjacencyMap,
            dataProcessor.idToCommit,
            dataProcessor.idToCommit
        )
    }

    override fun compareResults(result1: Map<String, Set<String>>, result2: Map<String, Set<String>>) =
        compareMapOfSets(result1, result2)

}