package visualisation.graph

import calculations.CoordinationNeedsMatrixCalculation
import dataProcessor.AssignmentMatrixDataProcessor
import dataProcessor.FileDependencyMatrixDataProcessor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import miners.gitMiners.FileDependencyMatrixMiner
import miners.gitMiners.UserChangedFilesMiner
import util.HeapNStorage
import util.HelpFunctionsUtil
import visualisation.entity.EdgeInfo
import visualisation.entity.EdgeThreeJS
import visualisation.entity.GraphDataThreeJS
import visualisation.entity.NodeThreeJS
import java.io.File

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
                EdgeThreeJS(
                    it.source,
                    it.target,
                    it.weight,
                    value = it.weight,
                    edgeColor(it.weight)
                )
            })
    }
}
