package miners.gitMiners

import dataProcessor.CoEditNetworksDataProcessor
import dataProcessor.inputData.CoEditInfo
import dataProcessor.inputData.entity.CommitInfo
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.revwalk.RevCommit
import util.ProjectConfig
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ConcurrentHashMap

// TODO: hot spots: read line, levenshtein
class CoEditNetworksMiner(
    repositoryFile: File,
    private val neededBranch: String,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) : GitMiner<CoEditNetworksDataProcessor>(repositoryFile, setOf(neededBranch), numThreads = numThreads) {

    private val threadLocalByteArrayOutputStream = object : ThreadLocal<ByteArrayOutputStream>() {
        override fun initialValue(): ByteArrayOutputStream {
            return ByteArrayOutputStream()
        }
    }

    private val threadLocalDiffFormatterWithBuffer = object : ThreadLocal<DiffFormatter>() {
        override fun initialValue(): DiffFormatter {
            return GitMinerUtil.getDiffFormatterWithBuffer(repository, threadLocalByteArrayOutputStream.get())
        }
    }

    private val prevAndNextCommit: ConcurrentHashMap<String, Pair<CommitInfo, CommitInfo>> = ConcurrentHashMap()

    // TODO: file_renaming, binary_file_change, cyclomatic_complexity
    override fun process(dataProcessor: CoEditNetworksDataProcessor, commit: RevCommit) {
        val reader = threadLocalReader.get()
        val out = threadLocalByteArrayOutputStream.get()
        val diffFormatter = threadLocalDiffFormatterWithBuffer.get()
        val edits = GitMinerUtil.getFileEdits(commit, reader, repository, out, diffFormatter)
        val (prevCommitInfo, nextCommitInfo) = prevAndNextCommit.computeIfAbsent(commit.name) { CommitInfo() to CommitInfo() }
        val commitInfo = CommitInfo(commit)
        val data = CoEditInfo(prevCommitInfo, commitInfo, nextCommitInfo, edits)

        dataProcessor.processData(data)
    }

    override fun run(dataProcessor: CoEditNetworksDataProcessor) {
        getPrevAndNextCommits()
        super.run(dataProcessor)
    }

    private fun getPrevAndNextCommits() {
        val git = threadLocalGit.get()
        val branch = GitMinerUtil.findNeededBranch(git, neededBranch)

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

}
