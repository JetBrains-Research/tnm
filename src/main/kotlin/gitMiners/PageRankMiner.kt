package gitMiners

import com.google.gson.Gson
import org.eclipse.jgit.api.BlameCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.Edit
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.util.io.DisabledOutputStream
import util.CommitMapper
import util.FileMapper
import util.Graph
import util.ProjectConfig
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.Executors


// Class based on paper:
// "An Application of the PageRank Algorithm"
// https://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=8051375&tag=1
class PageRankMiner(override val repository: FileRepository) : GitMiner {
    override val git = Git(repository)
    override val reader: ObjectReader = repository.newObjectReader()
    override val gson: Gson = Gson()

    private val diffFormatter = DiffFormatter(DisabledOutputStream.INSTANCE)

    init {
        diffFormatter.setRepository(repository)
        diffFormatter.setDiffComparator(RawTextComparator.DEFAULT)
        diffFormatter.isDetectRenames = true
    }

    // H is the transition probability matrix whose (i, j)
    // element signifies the probability of transition from the i-th page to the j-th page
    // pages - commits
    // TODO: probability == 1 ?
    private val commitsGraph = Graph<Int>()
    private val fixCommits = ArrayList<Pair<RevCommit, RevCommit>>()

    // TODO: skiplist set?
    private val concurrentGraph = ConcurrentHashMap<Int, ConcurrentSkipListSet<Int>>()

    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        CommitMapper.add(currCommit.name)
        CommitMapper.add(prevCommit.name)

        if (isBugFix(currCommit)) {
            fixCommits.add(prevCommit to currCommit)
        }
    }

    private fun getCommitsForLines(commit: RevCommit, fileName: String): List<String> {
        val result = ArrayList<String>()

        val blamer = BlameCommand(repository)
        blamer.setStartCommit(commit.id)
        blamer.setFilePath(fileName)
        val blame = blamer.call()

        val resultContents = blame.resultContents

        for (i in 0 until resultContents.size()) {
            val commitOfLine = blame.getSourceCommit(i)
            result.add(commitOfLine.name)
        }

        return result
    }

    override fun saveToJson() {
        File("./resources/commitsGraph").writeText(gson.toJson(commitsGraph.adjacencyMap))
        File("./resources/concurrentGraph").writeText(gson.toJson(concurrentGraph))
    }

    override fun run() {
        val branches: List<Ref> = git.branchList().call()

        for (branch in branches) {
            val commitsInBranch = git.log()
                .add(repository.resolve(branch.name))
                .call()
                .reversed()

            // TODO: first commit and empty tree
            for ((prevCommit, currCommit) in commitsInBranch.windowed(2)) {
                process(currCommit, prevCommit)
            }
        }

        multiThreadRun()
//        oneThreadRun()
    }

    private fun oneThreadRun() {
        for ((prevCommit, currCommit) in fixCommits) {
            val currCommitId = CommitMapper.add(currCommit.name)
            val prevCommitId = CommitMapper.add(prevCommit.name)

            commitsGraph.addNode(currCommitId)
            commitsGraph.addNode(prevCommitId)

            val diffs = getDiffs(currCommit, prevCommit)

            val commitsAdj = getCommitsAdj(diffs, prevCommit)
            for (commitId in commitsAdj) {
                commitsGraph.addEdge(currCommitId, commitId)
            }
        }
    }

    private fun getCommitsAdj(diffs: MutableList<DiffEntry>, prevCommit: RevCommit): MutableSet<Int> {
        val commitsAdj = mutableSetOf<Int>()
        val filesCommits = mutableMapOf<Int, List<String>>()
        for (diff in diffs) {
            if (diff.changeType != DiffEntry.ChangeType.MODIFY) continue
            val fileName = diff.oldPath
            val fileId = FileMapper.add(fileName)

            var prevCommitBlame = listOf<String>()

            if (!filesCommits.containsKey(fileId)) {
                prevCommitBlame = getCommitsForLines(prevCommit, fileName)
                filesCommits[fileId] = prevCommitBlame
            } else {
                val list = filesCommits[fileId]
                if (list != null) {
                    prevCommitBlame = list
                }
            }

            val editList = diffFormatter.toFileHeader(diff).toEditList()
            for (edit in editList) {
                if (edit.type != Edit.Type.REPLACE && edit.type != Edit.Type.DELETE) continue
                val lines = edit.beginA until edit.endA

                for (line in lines) {
                    val commitId = CommitMapper.add(prevCommitBlame[line])
                    commitsAdj.add(commitId)
                }
            }

        }
        return commitsAdj
    }

    private fun multiThreadRun(nThreads: Int = 5) {

        val executor = Executors.newFixedThreadPool(nThreads)

        for ((prevCommit, currCommit) in fixCommits) {
            val currCommitId = CommitMapper.add(currCommit.name)
            val prevCommitId = CommitMapper.add(prevCommit.name)

            commitsGraph.addNode(currCommitId)
            commitsGraph.addNode(prevCommitId)

            concurrentGraph.computeIfAbsent(currCommitId) { ConcurrentSkipListSet() }

            val worker = Runnable {
                val diffFormatter = DiffFormatter(DisabledOutputStream.INSTANCE)
                diffFormatter.setRepository(repository)
                diffFormatter.setDiffComparator(RawTextComparator.DEFAULT)
                diffFormatter.isDetectRenames = true

                val git = Git(repository)
                val reader: ObjectReader = repository.newObjectReader()

                val oldTreeIter = CanonicalTreeParser()
                oldTreeIter.reset(reader, prevCommit.tree)

                val newTreeIter = CanonicalTreeParser()
                newTreeIter.reset(reader, currCommit.tree)

                val diffs = git.diff()
                    .setNewTree(newTreeIter)
                    .setOldTree(oldTreeIter)
                    .call()

                val commitsAdj = getCommitsAdj(diffs, prevCommit)
                for (commitId in commitsAdj) {
                    concurrentGraph.computeIfAbsent(currCommitId) { ConcurrentSkipListSet() }
                        .add(commitId)
                }
            }
            executor.execute(worker)
        }

        executor.shutdown()
        while (!executor.isTerminated) {
        }
    }


    private fun isBugFix(commit: RevCommit): Boolean {
        return "fix" in commit.shortMessage.toLowerCase()
    }
}

fun main() {
    val m = PageRankMiner(ProjectConfig.repository)
    m.run()
    m.saveToJson()
    CommitMapper.saveToJson()
}