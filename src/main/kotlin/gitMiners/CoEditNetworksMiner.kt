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
import util.serialization.ConcurrentSkipListSetSerializer
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet

// TODO: hot spots: read line, levenshtein
class CoEditNetworksMiner(
    repository: FileRepository,
    neededBranches: Set<String> = ProjectConfig.DEFAULT_NEEDED_BRANCHES,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) : GitMiner(repository, neededBranches, numThreads = numThreads) {
    companion object {
        private const val ADD_MARK = '+'
        private const val DELETE_MARK = '-'
        private const val DIFF_MARK = '@'
        private val regex = Regex("@@ -(\\d+)(,\\d+)? \\+(\\d+)(,\\d+)? @@")
    }

    private val result = ConcurrentSkipListSet<CommitResult>()
    private val serializer = ConcurrentSkipListSetSerializer(CommitResult.serializer())

    enum class ChangeType {
        ADD, DELETE, REPLACE, EMPTY
    }

    @Serializable
    class CommitResult(
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

    // TODO: file_renaming and binary_file_change
    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        val git = Git(repository)
        val reader = repository.newObjectReader()
        // get all diffs and then proceed separately
        val diffs = UtilGitMiner.getDiffsWithoutText(currCommit, prevCommit, reader, git)
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
            val oldPathId = getFileId(diff.oldPath)
            val newPathId = getFileId(diff.newPath)

            for (line in diffText) {
                if (line.isEmpty()) continue

                val mark = line[0]
                // pass until diffs
                if (!start && mark == DIFF_MARK) {
                    start = true
                }

                when (mark) {
                    ADD_MARK -> {
                        if (start) {
                            addBlock.add(line.substring(1))
                        }
                    }
                    DELETE_MARK -> {
                        if (start) {
                            deleteBlock.add(line.substring(1))
                        }
                    }
                    DIFF_MARK -> {
                        generateEdit(
                            addBlock, deleteBlock, preStartLineNum,
                            postStartLineNum, oldPathId, newPathId
                        )?.let {
                            edits.add(it)
                            addBlock.clear()
                            deleteBlock.clear()
                        }

                        val match = regex.find(line)
                        preStartLineNum = match!!.groupValues[1].toInt()
                        postStartLineNum = match.groupValues[3].toInt()
                    }
                }


            }


            generateEdit(
                addBlock, deleteBlock, preStartLineNum,
                postStartLineNum, oldPathId, newPathId
            )?.let {
                edits.add(it)
                addBlock.clear()
                deleteBlock.clear()
            }


            out.reset()
        }
        out.reset()

        val info = CommitInfo(currCommit)

        val currCommitId = CommitMapper.add(currCommit.name)
        result.add(CommitResult(currCommitId, info, edits))
    }


    private fun getFileId(path: String): Int {
        if (path == DiffEntry.DEV_NULL) return -1
        // delete prefixes a/, b/ of DiffFormatter
        return FileMapper.add(path.substring(2))
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

    private fun generateEdit(
        addBlock: List<String>,
        deleteBlock: List<String>,
        preStartLineNum: Int,
        postStartLineNum: Int,
        oldPath: Int,
        newPath: Int
    ): Edit? {

        val type = getType(addBlock, deleteBlock)
        if (type == ChangeType.EMPTY) return null

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
        return Edit(
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
            result, serializer
        )
        Mapper.saveAll(resourceDirectory)
    }

}

fun main() {
//    val repo = FileRepository("../test_repo_1/.git")
    val repo = FileRepository("../repos/react/.git")
    val miner = CoEditNetworksMiner(repo, setOf("master"), numThreads = 6)
    miner.run()
    miner.saveToJson(File("./resources"))
}
