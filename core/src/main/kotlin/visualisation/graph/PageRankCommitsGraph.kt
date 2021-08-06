package visualisation.graph

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import util.HeapNStorage
import visualisation.entity.EdgeThreeJS
import visualisation.entity.GraphDataThreeJS
import visualisation.entity.NodeInfo
import visualisation.entity.NodeThreeJS
import java.io.File

class PageRankCommitsGraph(
    val pageRank: Map<Int, Float>,
    val commitInfluence: Map<Int, Set<Int>>,
    val idToCommit: Map<Int, String> = HashMap()
) : GraphThreeJS("graphPageRankCommits.js") {
    override fun generateData(size: Int, descending: Boolean): GraphDataThreeJS {
        val comparator = if (descending) compareByDescending<NodeInfo> { it.value } else compareBy { it.value }
        val nodeStorage = HeapNStorage(size, comparator)

        for (entry in pageRank) {
            nodeStorage.add(
                NodeInfo(entry.key, entry.value)
            )
        }

        val nodesIds = mutableSetOf<Int>()
        val nodes = nodeStorage.map {
            nodesIds.add(it.id)
            if (commitInfluence[it.id] != null) {
                val value = pageRank[it.id] ?: throw Exception("Can't find ")
                val commitId = getCommitId(it.id)
                NodeThreeJS(
                    commitId,
                    value,
                    shape = 1
                )
            } else {
                NodeThreeJS(
                    getCommitId(it.id),
                    it.value
                )
            }
        }

        val edges = mutableListOf<EdgeThreeJS>()
        for (nodeInfo in nodeStorage) {
            val adjCommits = commitInfluence[nodeInfo.id] ?: emptySet()
            // TODO: not mentioned nodes in link?
            edges.addAll(
                adjCommits.filter {
                    it in nodesIds
                }
                .map {
                    EdgeThreeJS(
                        getCommitId(nodeInfo.id),
                        getCommitId(it),
                        0.5f,
                        0.5f
                    )
                })
        }


        return GraphDataThreeJS(nodes.toList(), edges.toList())
    }

    private fun getCommitId(key: Int): String = idToCommit[key] ?: "commit: $key"
}

