package miners.gitMiners

import TestConfig.branch
import TestConfig.gitDir
import dataProcessor.CoEditNetworksDataProcessor
import dataProcessor.CoEditNetworksDataProcessor.ChangeType
import dataProcessor.CoEditNetworksDataProcessor.CommitInfoEncoded
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.SetSerializer
import kotlin.test.assertTrue

class CoEditNetworksMinerTests : GitMinerTest<Set<CoEditNetworksMinerTests.CommitResultWithoutId>>() {

    override val serializer = SetSerializer(CommitResultWithoutId.serializer())

    override fun compareResults(result1: Set<CommitResultWithoutId>, result2: Set<CommitResultWithoutId>) =
        compareSets(result1, result2)

    override fun runMiner(numThreads: Int): Set<CommitResultWithoutId> {
        val dataProcessor = CoEditNetworksDataProcessor()
        val miner = CoEditNetworksMiner(gitDir, numThreads = numThreads, neededBranch = branch)
        miner.run(dataProcessor)

        assertTrue(dataProcessor.coEdits.isNotEmpty())

        return replaceIds(dataProcessor)
    }

    @Serializable
    data class CommitResultWithoutId(
        val prevCommitInfo: CommitInfoWithoutId,
        val commitInfo: CommitInfoWithoutId,
        val nextCommitInfo: CommitInfoWithoutId,
        val edits: List<EditWithoutId>
    )

    @Serializable
    data class CommitInfoWithoutId(
        val hash: String,
        val author: String,
        val date: Long
    ) {
        constructor(
            commitInfoEncoded: CommitInfoEncoded,
            idToCommit: Map<Int, String>,
            idToUser: Map<Int, String>
        ) :
                this(
                    idToCommit[commitInfoEncoded.commitId]!!,
                    idToUser[commitInfoEncoded.userId]!!,
                    commitInfoEncoded.date
                )
    }

    @Serializable
    data class EditWithoutId(
        val oldPath: String,
        val newPath: String,
        val preStartLineNum: Int,
        val postStartLineNum: Int,
        val preLenInLines: Int,
        val postLenInLines: Int,
        val preLenInChars: Int,
        val postLenInChars: Int,
        val preEntropy: Double,
        val postEntropy: Double,
        val levenshtein: Int,
        val type: ChangeType
    )

    private fun replaceIds(
        dataProcessor: CoEditNetworksDataProcessor
    ): Set<CommitResultWithoutId> {
        val result = mutableSetOf<CommitResultWithoutId>()

        val set = dataProcessor.coEdits
        val idToUser = dataProcessor.idToUser
        val idToFile = dataProcessor.idToFile
        val idToCommit = dataProcessor.idToCommit

        for (commitResult in set) {

            val edits = mutableListOf<EditWithoutId>()
            for (edit in commitResult.editsData) {
                val oldPath = idToFile[edit.oldPathId]!!
                val newPath = idToFile[edit.newPathId]!!

                edits.add(
                    EditWithoutId(
                        oldPath,
                        newPath,
                        edit.preStartLineNum,
                        edit.postStartLineNum,
                        edit.preLenInLines,
                        edit.postLenInLines,
                        edit.preLenInChars,
                        edit.postLenInChars,
                        edit.preEntropy,
                        edit.postEntropy,
                        edit.levenshtein,
                        edit.type
                    )
                )
            }

            result.add(
                CommitResultWithoutId(
                    CommitInfoWithoutId(commitResult.prevCommitInfoEncoded, idToCommit, idToUser),
                    CommitInfoWithoutId(commitResult.commitInfoEncoded, idToCommit, idToUser),
                    CommitInfoWithoutId(commitResult.nextCommitInfoEncoded, idToCommit, idToUser),
                    edits
                )
            )

        }

        return result
    }
}
