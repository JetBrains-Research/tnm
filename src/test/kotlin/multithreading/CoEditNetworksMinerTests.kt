package multithreading

import GitMinerNewTest
import GitMinerNewTest.Companion.repository
import GitMinerTest.Companion.repositoryDir
import dataProcessor.CoEditNetworksDataProcessor
import dataProcessor.CoEditNetworksDataProcessor.*
import gitMiners.CoEditNetworksMiner
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.junit.Test
import util.ProjectConfig
import java.io.File
import kotlin.test.assertTrue

class CoEditNetworksMinerTests : GitMinerNewTest {
    @Test
    fun `test one thread and multithreading`() {
        val resultOneThread = runMiner(1)
        val resultMultithreading = runMiner()

        compareSets(resultOneThread, resultMultithreading)
    }

    private fun runMiner(numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS): Set<CommitResultWithoutId> {
        val dataProcessor = CoEditNetworksDataProcessor()
        val miner = CoEditNetworksMiner(repository, numThreads = numThreads)
        miner.run(dataProcessor)

        assertTrue(dataProcessor.coEdits.isNotEmpty())

        return replaceIds(dataProcessor)
    }

    data class CommitResultWithoutId(
        val prevCommitInfo: CommitInfoWithoutId,
        val commitInfo: CommitInfoWithoutId,
        val nextCommitInfo: CommitInfoWithoutId,
        val edits: List<EditWithoutId>
    )

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
        val idToUser = dataProcessor.userMapper.idToUser
        val idToFile = dataProcessor.fileMapper.idToFile
        val idToCommit = dataProcessor.commitMapper.idToCommit

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
