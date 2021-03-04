package cli.visualizationCLI

import cli.InfoCLI
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import visualisation.GraphHTML.EdgeInfo
import visualisation.GraphHTML.NodeInfo
import visualisation.WeightedEdgesGraphHTML
import java.io.File
import java.util.*
import kotlin.collections.HashMap


class WeightedEdgesGraphCLI : VisualizationCLI(
    InfoCLI(
        "WeightedEdgesGraph",
        "Create graph with weighted edges in html format"
    )
) {
    private val reverse by option("-r", "--reverse", help = "Store edges from min to max.")
        .flag(default = false)
    private val numberOfEdges by option("-n", "--max-number-of-edges", help = "Max number of edges.")
        .int()
        .default(Int.MAX_VALUE)

    override fun run() {
        val (priorityQueue, max) = storeNEdgesAndMax(graph, numberOfEdges, reverse)
        val (nodes, edges) = createEdges(priorityQueue, max)
        val graph = WeightedEdgesGraphHTML(nodes, edges)
        graph.draw(name, File("$name.html"))
    }

    private fun storeNEdgesAndMax(
        file: File,
        N: Int,
        reverse: Boolean = false
    ): Pair<PriorityQueue<Triple<String, String, Float>>, Float> {
        val adjacencyMap = Json.decodeFromString<HashMap<String, HashMap<String, Float>>>(file.readText())
        var max = 0.0f

        val compareWeight: Comparator<Triple<String, String, Float>> = if (!reverse) {
            compareBy { it.third }
        } else {
            compareByDescending { it.third }
        }


        val priorityQueue = PriorityQueue(compareWeight)
        val storedEdges = HashMap<String, MutableSet<String>>()

        for (entry1 in adjacencyMap) {
            val nodeFrom = entry1.key
            for (entry2 in entry1.value) {
                if (priorityQueue.size > N) priorityQueue.remove()
                val nodeTo = entry2.key
                val weight = entry2.value

                if (!storedEdges.computeIfAbsent(nodeFrom) { mutableSetOf() }.contains(nodeTo)) {
                    priorityQueue.add(Triple(nodeFrom, nodeTo, weight))
                    max = maxOf(max, weight)
                }

                storedEdges.computeIfAbsent(nodeFrom) { mutableSetOf() }.add(nodeTo)
                storedEdges.computeIfAbsent(nodeTo) { mutableSetOf() }.add(nodeFrom)
            }
        }
        return priorityQueue to max
    }

    private fun edgeColor(
        weight: Float,
        quantile1: String = "#0569E1",
        quantile2: String = "#C1F823",
        quantile3: String = "#FCAA05",
        quantile4: String = "#EE5503"
    ): String {
        if (weight <= 0.25f) return quantile1
        if (weight <= 0.5f) return quantile2
        if (weight <= 0.75f) return quantile3
        return quantile4
    }

    private fun createEdges(
        priorityQueue: PriorityQueue<Triple<String, String, Float>>,
        max: Float
    ): Pair<Set<NodeInfo>, Set<EdgeInfo>> {
        val nodes = mutableSetOf<NodeInfo>()
        val edges = mutableSetOf<EdgeInfo>()


        while (priorityQueue.isNotEmpty()) {
            val (from, to, weight) = priorityQueue.remove()
            edges.add(EdgeInfo(from, to, edgeColor(weight / max), weight.toString()))
            nodes.add(NodeInfo(from))
            nodes.add(NodeInfo(to))
        }

        return nodes to edges
    }
}
