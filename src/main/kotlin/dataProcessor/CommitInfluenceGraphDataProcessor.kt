package dataProcessor

import dataProcessor.CommitInfluenceGraphDataProcessor.AddData
import util.CommitMapper
import util.FileMapper
import util.Graph
import util.UserMapper

class CommitInfluenceGraphDataProcessor: DataProcessor<AddData> {

    override val userMapper = UserMapper()
    override val fileMapper = FileMapper()
    override val commitMapper = CommitMapper()

    // H is the transition probability matrix whose (i, j)
    // element signifies the probability of transition from the i-th page to the j-th page
    // pages - commits
    private val commitsGraph = Graph<Int>()

    val adjacencyMap : Map<Int, Set<Int>>
        get() = commitsGraph.adjacencyMap

    data class AddData(val bugFixCommit: String, val prevCommit: String, val adjCommits: Set<String>)

    override fun processData(data: AddData) {
        val bugFixCommitId = commitMapper.add(data.bugFixCommit)
        val prevCommitId = commitMapper.add(data.prevCommit)

        commitsGraph.addNode(bugFixCommitId)
        commitsGraph.addNode(prevCommitId)

        for (commit in data.adjCommits) {
            val commitId = commitMapper.add(commit)
            commitsGraph.addEdge(bugFixCommitId, commitId)
        }
    }

    override fun calculate() {}

}