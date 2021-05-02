package dataProcessor

import dataProcessor.inputData.CommitInfluenceInfo
import util.Graph

class CommitInfluenceGraphDataProcessor : DataProcessorMapped<CommitInfluenceInfo>() {
    // H is the transition probability matrix whose (i, j)
    // element signifies the probability of transition from the i-th page to the j-th page
    // pages - commits
    private val commitsGraph = Graph<Int>()

    val adjacencyMap: Map<Int, Set<Int>>
        get() = commitsGraph.adjacencyMap


    override fun processData(data: CommitInfluenceInfo) {
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