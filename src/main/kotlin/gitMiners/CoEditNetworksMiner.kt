package gitMiners

import kotlinx.serialization.Serializable
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevCommit
import util.CommitMapper
import util.ProjectConfig
import util.UserMapper
import util.UtilFunctions
import util.UtilFunctions.entropy
import util.UtilFunctions.levenshtein
import java.io.ByteArrayOutputStream
import java.io.File


class CoEditNetworksMiner(
    repository: FileRepository,
    neededBranches: Set<String> = ProjectConfig.neededBranches,
) : GitMiner(repository, neededBranches, numThreads = 1) {
    companion object {
        //        private const val NOT_NEWLINE = "\\ No newline at end of file"
        private const val NOT_NEWLINE = "\\"
        private const val ADD_MARK = '+'
        private const val DELETE_MARK = '-'
        private const val NO_MARK = ' '
        private const val DIFF_MARK = '@'
        private val regex = Regex("@@ -(\\d+)(,\\d+)? \\+(\\d+)(,\\d+)? @@")
    }

    private val out = ByteArrayOutputStream()
    private val diffFormatter = DiffFormatter(out)
    private val result = mutableSetOf<List<Edit>>()


    init {
        diffFormatter.setRepository(repository)
        diffFormatter.setDiffComparator(RawTextComparator.DEFAULT)
        diffFormatter.isDetectRenames = true
        diffFormatter.setContext(0)
    }

    enum class ChangeType {
        ADD, DELETE, REPLACE, EMPTY
    }

    @Serializable
    data class Edit(
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

    //    TODO: file_renaming and binary_file_change
    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        val diffs = UtilGitMiner.getDiffs(currCommit, prevCommit, reader, git)
        val email = currCommit.authorIdent.emailAddress

        val userId = UserMapper.add(email)
        val currCommitId = CommitMapper.add(currCommit.name)
        val prevCommitId = CommitMapper.add(prevCommit.name)

        val deleteBlock = mutableListOf<String>()
        val addBlock = mutableListOf<String>()
        val edits = mutableListOf<Edit>()
        for (diff in diffs) {
            var start = false
            // TODO: find better solution inside format method
            diffFormatter.format(diff)
            val diffText = out.toString("UTF-8").split("\n")
            var preStartLineNum = 0
            var postStartLineNum = 0

            for (line in diffText) {
                if (line.isEmpty()) continue

                val mark = line[0]
                // pass until diffs
                if (!start && mark == DIFF_MARK) {
                    start = true
                }
                if (!start) continue

                when (mark) {
                    ADD_MARK -> addBlock.add(line.substring(1))
                    DELETE_MARK -> deleteBlock.add(line.substring(1))
                    DIFF_MARK -> {
                        processBlocks(addBlock, deleteBlock, edits, preStartLineNum, postStartLineNum)
                        val match = regex.find(line)
                        preStartLineNum = match!!.groupValues[1].toInt()
                        postStartLineNum = match.groupValues[3].toInt()
                    }
                }


            }

            processBlocks(addBlock, deleteBlock, edits, preStartLineNum, postStartLineNum)

            out.reset()
        }
        out.reset()
        result.add(edits)
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
        postStartLineNum: Int
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
            result
        )
    }

}


fun main() {
    val repo = FileRepository("../test_repo_1/.git")
    val miner = CoEditNetworksMiner(repo, setOf("master"))
    miner.run()
    miner.saveToJson(File("./resources"))
}
