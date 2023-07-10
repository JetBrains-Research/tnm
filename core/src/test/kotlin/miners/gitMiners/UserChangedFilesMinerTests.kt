package miners.gitMiners

import TestConfig.branches
import TestConfig.gitDir
import dataProcessor.ChangedFilesDataProcessor
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.test.assertTrue

internal class UserChangedFilesMinerTests : GitMinerTest<Map<String, Set<String>>>() {

    override val serializer = MapSerializer(String.serializer(), SetSerializer(String.serializer()))

    override fun runMiner(numThreads: Int): Map<String, Set<String>> {
        val dataProcessor = ChangedFilesDataProcessor()
        val miner = UserChangedFilesMiner(gitDir, numThreads = numThreads, neededBranches = branches)
        miner.run(dataProcessor)

        assertTrue(dataProcessor.changedFilesByUsers.isNotEmpty())

        return changeIdsToValuesInMapOfSets(
            dataProcessor.changedFilesByUsers,
            dataProcessor.idToUser,
            dataProcessor.idToFile
        )
    }

    override fun compareResults(result1: Map<String, Set<String>>, result2: Map<String, Set<String>>) =
        compareMapOfSets(result1, result2)
}