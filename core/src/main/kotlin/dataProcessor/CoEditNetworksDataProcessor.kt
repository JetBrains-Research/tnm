package dataProcessor

import dataProcessor.inputData.CoEditInfo
import dataProcessor.inputData.entity.CommitInfo
import dataProcessor.inputData.entity.FileEdit
import kotlinx.serialization.Serializable
import org.apache.commons.text.similarity.LevenshteinDistance
import util.HelpFunctionsUtil
import util.mappers.CommitMapper
import util.mappers.UserMapper
import java.util.concurrent.ConcurrentSkipListSet

class CoEditNetworksDataProcessor : DataProcessorMapped<CoEditInfo>() {
    private val _coEdits = ConcurrentSkipListSet<CommitResult>()
    private val threadLocalLevenshteinDistance = object : ThreadLocal<LevenshteinDistance>() {
        override fun initialValue(): LevenshteinDistance {
            return LevenshteinDistance()
        }
    }

    val coEdits: Set<CommitResult>
        get() = _coEdits

    @Serializable
    data class CommitInfoEncoded(
        val commitId: Int,
        val userId: Int,
        val date: Long
    ) {
        constructor(commitInfo: CommitInfo, commitMapper: CommitMapper, userMapper: UserMapper) : this(
            commitMapper.add(commitInfo.hash),
            userMapper.add(commitInfo.author),
            commitInfo.date
        )
    }

    @Serializable
    data class EditData(
        val oldPathId: Int,
        val newPathId: Int,
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

    @Serializable
    class CommitResult(
        val prevCommitInfoEncoded: CommitInfoEncoded,
        val commitInfoEncoded: CommitInfoEncoded,
        val nextCommitInfoEncoded: CommitInfoEncoded,
        val editsData: List<EditData>
    ) : Comparable<CommitResult> {
        override fun compareTo(other: CommitResult): Int {
            return commitInfoEncoded.commitId.compareTo(other.commitInfoEncoded.commitId)
        }
    }

    enum class ChangeType {
        ADD, DELETE, REPLACE, EMPTY
    }

    override fun processData(data: CoEditInfo) {
        val editsData = mutableListOf<EditData>()

        for (edit in data.edits) {
            generateEditData(edit)?.let {
                editsData.add(it)
            }
        }

        val (prevCommitInfo, commitInfo, nextCommitInfo) = data

        val prevCommitInfoEncoded = CommitInfoEncoded(prevCommitInfo, commitMapper, userMapper)
        val commitInfoEncoded = CommitInfoEncoded(commitInfo, commitMapper, userMapper)
        val nextCommitInfoEncoded = CommitInfoEncoded(nextCommitInfo, commitMapper, userMapper)

        _coEdits.add(CommitResult(prevCommitInfoEncoded, commitInfoEncoded, nextCommitInfoEncoded, editsData))
    }

    override fun calculate() {}

    private fun generateEditData(
        edit: FileEdit
    ): EditData? {
        val (addBlock, deleteBlock, preStartLineNum, postStartLineNum, oldPath, newPath) = edit

        val type = getType(addBlock, deleteBlock)
        if (type == ChangeType.EMPTY) return null

        val deleteString = deleteBlock.joinToString("")
        val addString = addBlock.joinToString("")
        val preLenInLines: Int = deleteBlock.size
        val postLenInLines: Int = addBlock.size
        val preLenInChars = deleteString.length
        val postLenInChars = addString.length
        val preEntropy: Double = HelpFunctionsUtil.entropy(countUTF8(deleteBlock))
        val postEntropy: Double = HelpFunctionsUtil.entropy(countUTF8(addBlock))

        val levenshtein: Int = when (type) {
            ChangeType.ADD -> {
                postLenInChars
            }
            ChangeType.DELETE -> {
                preLenInChars
            }
            ChangeType.REPLACE -> {
                threadLocalLevenshteinDistance.get().apply(deleteString, addString)
            }
            else -> 0
        }

        val oldPathId = fileMapper.add(oldPath)
        val newPathId = fileMapper.add(newPath)

        return EditData(
            oldPathId,
            newPathId,
            preStartLineNum,
            postStartLineNum,
            preLenInLines,
            postLenInLines,
            preLenInChars,
            postLenInChars,
            preEntropy,
            postEntropy,
            levenshtein,
            type
        )

    }

    private fun getType(
        addBlock: List<String>,
        deleteBlock: List<String>
    ): ChangeType {

        when {
            addBlock.isNotEmpty() && deleteBlock.isEmpty() -> return ChangeType.ADD
            addBlock.isEmpty() && deleteBlock.isNotEmpty() -> return ChangeType.DELETE
            addBlock.isNotEmpty() && deleteBlock.isNotEmpty() -> return ChangeType.REPLACE
        }

        return ChangeType.EMPTY
    }

    private fun countUTF8(block: List<String>): Collection<Int> {
        val count = HashMap<Int, Int>()

        for (line in block) {
            for (char in line) {
                val id = char.code
                if (id < 256) {
                    count.compute(id) { _, v -> if (v == null) 1 else v + 1 }
                }
            }
        }

        return count.values
    }

}