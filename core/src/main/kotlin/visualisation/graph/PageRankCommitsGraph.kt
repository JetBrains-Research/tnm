package visualisation.graph

import util.HeapNStorage
import visualisation.entity.EdgeThreeJS
import visualisation.entity.GraphDataThreeJS
import visualisation.entity.NodeInfo
import visualisation.entity.NodeThreeJS

class PageRankCommitsGraph(
    val pageRank: Map<Int, Float>,
    val commitInfluence: Map<Int, Set<Int>>,
    val idToCommit: Map<Int, String> = HashMap(),
    val commitToMsg: Map<String, String> = HashMap()
) : GraphThreeJS("graphPageRankCommits.js") {
    override fun generateData(size: Int, descending: Boolean): GraphDataThreeJS {
        val comparator = if (descending) compareByDescending<NodeInfo> { it.value } else compareBy { it.value }
        val nodeStorage = HeapNStorage(size, comparator)
        nodeStorage.addAll(commitInfluence.filter { it.value.isNotEmpty() }.keys.map {
            NodeInfo(it, pageRank[it]!!)
        })

        val nodes = mutableListOf<NodeThreeJS>()
        val edges = mutableListOf<EdgeThreeJS>()

        val addedNodesIds = mutableSetOf<Int>()
        val bugFixIds = nodeStorage.map { it.id }

        for (bugId in bugFixIds) {
            val adjCommits = commitInfluence[bugId]!!
            val bugCommitJsId = getCommitJsId(bugId)
            val bugCommitValue = getValueOfCommit(bugId)

            if (!addedNodesIds.contains(bugId) && adjCommits.isNotEmpty()) {
                nodes.add(
                    bugFixCommitNode(
                        bugCommitJsId,
                        bugCommitValue
                    )
                )
                addedNodesIds.add(bugId)
            }

            for (targetId in adjCommits) {
                val targetCommitJsId = getCommitJsId(targetId)
                val targetCommitValue = getValueOfCommit(targetId)

                edges.add(
                    EdgeThreeJS(
                        bugCommitJsId,
                        targetCommitJsId,
                        0.5f,
                        0.5f
                    )
                )

                if (addedNodesIds.add(targetId)) {
                    if (targetId in bugFixIds) {
                        nodes.add(bugFixCommitNode(targetCommitJsId, targetCommitValue))
                    } else {
                        nodes.add(commitNode(targetCommitJsId, targetCommitValue))
                    }
                }

            }
        }

        return GraphDataThreeJS(nodes.toList(), edges.toList())
    }

    private fun getValueOfCommit(id: Int) = 3 * pageRank[id]!!

    private fun getCommitJsId(key: Int): String = idToCommit[key] ?: "commit: $key"

    private fun bugFixCommitNode(commitId: String, value: Float): NodeThreeJS =
        NodeThreeJS(
            commitId,
            value,
            shape = 1,
            color = "#ff0000",
        )

    private fun commitNode(commitId: String, value: Float) =
        NodeThreeJS(
            commitId,
            value,
            msg = commitToMsg[commitId] ?: commitId
        )
}
