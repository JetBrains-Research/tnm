package gitMiners

import kotlinx.serialization.Serializable
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevCommit
import util.*
import util.UtilFunctions.entropy
import util.UtilFunctions.levenshtein
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet


class CoEditNetworksMiner(
    repository: FileRepository,
    neededBranches: Set<String> = ProjectConfig.neededBranches,
    numThreads: Int = ProjectConfig.numThreads
) : GitMiner(repository, neededBranches, numThreads = numThreads) {
    companion object {
        private const val ADD_MARK = '+'
        private const val DELETE_MARK = '-'
        private const val DIFF_MARK = '@'
        private const val OLD_PATH_MARK = "---"
        private const val NEW_PATH_MARK = "+++"
        private val regex = Regex("@@ -(\\d+)(,\\d+)? \\+(\\d+)(,\\d+)? @@")
    }

    private val result = ConcurrentSkipListSet<CommitResult>()

    enum class ChangeType {
        ADD, DELETE, REPLACE, EMPTY
    }

    @Serializable
    data class CommitResult(
        val id: Int,
        val info: CommitInfo,
        val edits: List<Edit>
    ) : Comparable<CommitResult> {
        override fun compareTo(other: CommitResult): Int {
            return id.compareTo(other.id)
        }

    }

    // TODO: rename
    @Serializable
    data class Edit(
        val oldPath: Int,
        val newPath: Int,
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
    data class CommitInfo(
        val author: Int,
        val date: Long
    ) {
        constructor(commit: RevCommit)
                : this(
            UserMapper.add(commit.authorIdent.emailAddress),
            commit.commitTime * 1000L
        )
    }

    //    TODO: file_renaming and binary_file_change
    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        val git = Git(repository)
        val reader = repository.newObjectReader()
        val diffs = UtilGitMiner.getDiffs(currCommit, prevCommit, reader, git)
//        TODO: make thread local?
        val out = ByteArrayOutputStream()

        val deleteBlock = mutableListOf<String>()
        val addBlock = mutableListOf<String>()
        val edits = mutableListOf<Edit>()

        val diffFormatter = DiffFormatter(out)
        diffFormatter.setRepository(repository)
        diffFormatter.setDiffComparator(RawTextComparator.DEFAULT)
        diffFormatter.isDetectRenames = true
        diffFormatter.setContext(0)

        for (diff in diffs) {
            var start = false
            diffFormatter.format(diff)
            val diffText = out.toString("UTF-8").split("\n")

            var preStartLineNum = 0
            var postStartLineNum = 0
            var oldPathId = -1
            var newPathId = -1

            for (line in diffText) {
                if (line.isEmpty()) continue

                val mark = line[0]
                // pass until diffs
                if (!start && mark == DIFF_MARK) {
                    start = true
                }
//                if (!start) continue

                when (mark) {
                    ADD_MARK -> {
                        if (start) {
                            addBlock.add(line.substring(1))
                        } else {
                            newPathId = getFileId(line, NEW_PATH_MARK)
                        }
                    }
                    DELETE_MARK -> {
                        if (start) {
                            deleteBlock.add(line.substring(1))
                        } else {
                            oldPathId = getFileId(line, OLD_PATH_MARK)
                        }
                    }
                    DIFF_MARK -> {
                        processBlocks(
                            addBlock,
                            deleteBlock,
                            edits,
                            preStartLineNum,
                            postStartLineNum,
                            oldPathId,
                            newPathId
                        )
                        val match = regex.find(line)
                        preStartLineNum = match!!.groupValues[1].toInt()
                        postStartLineNum = match.groupValues[3].toInt()
                    }
                }


            }

            processBlocks(addBlock, deleteBlock, edits, preStartLineNum, postStartLineNum, oldPathId, newPathId)

            out.reset()
        }
        out.reset()

        val info = CommitInfo(currCommit)

        val currCommitId = CommitMapper.add(currCommit.name)
        result.add(CommitResult(currCommitId, info, edits))
    }


    private fun getFileId(line: String, mark: String): Int {
        val path = line.substring(mark.length + 1)
        if (path == DiffEntry.DEV_NULL) return -1
//        delete prefixes a/, b/ of DiffFormatter
        return FileMapper.add(path.substring(2))
    }

    private fun getType(
        addBlock: List<String>,
        deleteBlock: List<String>
    ): ChangeType {

        if (addBlock.isNotEmpty() && deleteBlock.isEmpty()) return ChangeType.ADD
        if (addBlock.isEmpty() && deleteBlock.isNotEmpty()) return ChangeType.DELETE
        if (addBlock.isNotEmpty() && deleteBlock.isNotEmpty()) return ChangeType.REPLACE

        return ChangeType.EMPTY
    }

    private fun processBlocks(
        addBlock: MutableList<String>,
        deleteBlock: MutableList<String>,
        edits: MutableList<Edit>,
        preStartLineNum: Int,
        postStartLineNum: Int,
        oldPath: Int,
        newPath: Int
    ) {

        val type = getType(addBlock, deleteBlock)
        if (type == ChangeType.EMPTY) return

        val deleteString = deleteBlock.joinToString("")
        val addString = addBlock.joinToString("")
        val preLenInLines: Int = deleteBlock.size
        val postLenInLines: Int = addBlock.size
        val preLenInChars = deleteString.length
        val postLenInChars = addString.length
        val preEntropy: Double = entropy(countUTF8(deleteBlock))
        val postEntropy: Double = entropy(countUTF8(addBlock))

        val levenshtein: Int = when (type) {
            ChangeType.ADD -> {
                postLenInChars
            }
            ChangeType.DELETE -> {
                preLenInChars
            }
            ChangeType.REPLACE -> {
                levenshtein(deleteString, addString)
            }
            else -> 0
        }
        val edit = Edit(
            oldPath,
            newPath,
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
        edits.add(edit)

        addBlock.clear()
        deleteBlock.clear()
    }

    private fun countUTF8(block: List<String>): Collection<Int> {
        val count = HashMap<Int, Int>()

        for (line in block) {
            for (char in line) {
                val id = char.toInt()
                if (id < 256) {
                    count.compute(id) { _, v -> if (v == null) 1 else v + 1 }
                }
            }
        }

        return count.values
    }

    override fun saveToJson(resourceDirectory: File) {
        UtilFunctions.saveToJson(
            File(resourceDirectory, ProjectConfig.CO_EDIT),
            result.toSet()
        )
    }

}
