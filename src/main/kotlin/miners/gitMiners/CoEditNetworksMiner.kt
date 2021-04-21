package miners.gitMiners

import dataProcessor.CoEditNetworksDataProcessor
import dataProcessor.CoEditNetworksDataProcessor.*
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevCommit
import util.ProjectConfig
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap

// TODO: hot spots: read line, levenshtein
class CoEditNetworksMiner(
    repository: FileRepository,
    private val neededBranch: String,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) : GitMiner<CoEditNetworksDataProcessor>(repository, setOf(neededBranch), numThreads = numThreads) {
    companion object {
        private const val ADD_MARK = '+'
        private const val DELETE_MARK = '-'
        private const val DIFF_MARK = '@'
        private val regex = Regex("@@ -(\\d+)(,\\d+)? \\+(\\d+)(,\\d+)? @@")

    }

    private val threadLocalByteArrayOutputStream = object : ThreadLocal<ByteArrayOutputStream>() {
        override fun initialValue(): ByteArrayOutputStream {
            return ByteArrayOutputStream()
        }
    }

    private val threadLocalDiffFormatterWithBuffer = object : ThreadLocal<DiffFormatter>() {
        override fun initialValue(): DiffFormatter {
            val diffFormatter = DiffFormatter(threadLocalByteArrayOutputStream.get())
            diffFormatter.setRepository(repository)
            diffFormatter.setDiffComparator(RawTextComparator.DEFAULT)
            diffFormatter.isDetectRenames = true
            diffFormatter.setContext(0)
            return diffFormatter
        }
    }

    private val prevAndNextCommit: ConcurrentHashMap<String, Pair<CommitInfo, CommitInfo>> = ConcurrentHashMap()

    // TODO: file_renaming, binary_file_change, cyclomatic_complexity
    override fun process(dataProcessor: CoEditNetworksDataProcessor, currCommit: RevCommit, prevCommit: RevCommit) {
        val git = threadLocalGit.get()
        val reader = threadLocalReader.get()

        // get all diffs and then proceed separately
        val diffs = reader.let {
            UtilGitMiner.getDiffsWithoutText(currCommit, prevCommit, it, git)
        }

        val edits = mutableListOf<Edit>()

        val out = threadLocalByteArrayOutputStream.get()
        val diffFormatter = threadLocalDiffFormatterWithBuffer.get()

        for (diff in diffs) {
            val deleteBlock = mutableListOf<String>()
            val addBlock = mutableListOf<String>()

            var start = false
            diffFormatter.format(diff)
            val diffText = out.toString("UTF-8").split("\n")
            var preStartLineNum = 0
            var postStartLineNum = 0

            val oldPath = getFilePath(diff.oldPath)
            val newPath = getFilePath(diff.newPath)

            for (line in diffText) {
                if (line.isEmpty()) continue

                val mark = line[0]

                // pass until diffs
                if (!start) {
                    if (mark == DIFF_MARK) {
                        start = true
                    } else {
                        continue
                    }
                }

                when (mark) {
                    ADD_MARK -> {
                        addBlock.add(line.substring(1))
                    }
                    DELETE_MARK -> {
                        deleteBlock.add(line.substring(1))
                    }
                    DIFF_MARK -> {
                        if (addBlock.isNotEmpty() || deleteBlock.isNotEmpty()) {
                            val data = Edit(
                                addBlock.toList(), deleteBlock.toList(), preStartLineNum,
                                postStartLineNum, oldPath, newPath
                            )
                            edits.add(data)

                            addBlock.clear()
                            deleteBlock.clear()
                        }

                        val match = regex.find(line)!!
                        preStartLineNum = match.groupValues[1].toInt()
                        postStartLineNum = match.groupValues[3].toInt()
                    }
                }

            }

            val data = Edit(
                addBlock, deleteBlock, preStartLineNum,
                postStartLineNum, oldPath, newPath
            )
            edits.add(data)


            out.reset()
        }

        val hashCurr = currCommit.name
        val (prevCommitInfo, nextCommitInfo) = prevAndNextCommit.computeIfAbsent(hashCurr) { CommitInfo() to CommitInfo() }
        val commitInfo = CommitInfo(currCommit)

        val data = AddEntity(prevCommitInfo, commitInfo, nextCommitInfo, edits)

        dataProcessor.processData(data)
    }

    override fun run(dataProcessor: CoEditNetworksDataProcessor) {
        getPrevAndNextCommits()
        super.run(dataProcessor)
    }

    private fun getPrevAndNextCommits() {
        val git = threadLocalGit.get()
        val branch = UtilGitMiner.findNeededBranch(git, neededBranch)

        val commitsInBranch = getUnprocessedCommits(branch.name)
        for ((next, curr, prev) in commitsInBranch.windowed(3)) {
            val hashCurr = curr.name
            prevAndNextCommit[hashCurr] =
                CommitInfo(prev) to CommitInfo(next)
        }

        when {
            commitsInBranch.size > 1 -> {
                val (first, second) = commitsInBranch.take(2)
                val hashFirst = first.name
                prevAndNextCommit[hashFirst] = CommitInfo() to CommitInfo(second)

                val (preLast, last) = commitsInBranch.takeLast(2)
                val hashLast = last.name
                prevAndNextCommit[hashLast] = CommitInfo(preLast) to CommitInfo()
            }
            commitsInBranch.size == 1 -> {
                val commit = commitsInBranch.first()
                val hashCommit = commit.name
                prevAndNextCommit[hashCommit] = CommitInfo() to CommitInfo()
            }
        }
    }

    private fun getFilePath(path: String): String {
        if (path == DiffEntry.DEV_NULL) return ""
        // delete prefixes a/, b/ of DiffFormatter
        return path.substring(2)
    }

}
