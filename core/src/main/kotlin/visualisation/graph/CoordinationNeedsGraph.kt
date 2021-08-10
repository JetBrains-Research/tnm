package visualisation.graph

import util.HeapNStorage
import visualisation.entity.EdgeInfo
import visualisation.entity.EdgeThreeJS
import visualisation.entity.GraphDataThreeJS
import visualisation.entity.NodeThreeJS

class CoordinationNeedsGraph(
    val data: Array<out FloatArray>,
    val idToUser: Map<Int, String> = HashMap(),
) :
    GraphThreeJS("graphCN.js") {

    override fun generateData(size: Int, descending: Boolean): GraphDataThreeJS {
        val comparator = if (descending) compareByDescending<EdgeInfo> { it.weight } else compareBy { it.weight }
        val edgeStorage = HeapNStorage(size, comparator)
        for (i in 0..data.size) {
            for (j in i + 1 until data.size) {
                val user1 = idToUser[i] ?: "user: $i"
                val user2 = idToUser[j] ?: "user: $j"

                edgeStorage.add(
                    EdgeInfo(
                        user1,
                        user2,
                        data[i][j]
                    )
                )
            }
        }

        val nodes = mutableSetOf<NodeThreeJS>()
        for (edge in edgeStorage) {
            nodes.add(NodeThreeJS(edge.source))
            nodes.add(NodeThreeJS(edge.target))
        }

        return GraphDataThreeJS(
            nodes.toList(),
            edgeStorage.map {
                val value =
                    normalizeMinMax(it.weight, edgeStorage.low!!.weight, edgeStorage.high!!.weight)
                EdgeThreeJS(
                    it.source,
                    it.target,
                    it.weight,
                    value = value,
                    edgeColor(value)
                )
            })
    }
}
