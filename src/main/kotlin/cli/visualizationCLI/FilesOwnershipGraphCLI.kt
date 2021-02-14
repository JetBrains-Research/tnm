package cli.visualizationCLI

import cli.CLI
import cli.InfoCLI
import cli.UtilCLI
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import gitMiners.FilesOwnershipMiner
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.internal.storage.file.FileRepository
import util.ProjectConfig
import visualisation.EdgeInfo
import visualisation.GraphHTML
import visualisation.NodeInfo
import visualisation.WeightedEdgesGraphHTML
import java.io.File
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.pow

// TODO: rewrite logic, size of nodes
class FilesOwnershipGraphCLI : VisualizationCLI(
    InfoCLI("FilesOwnershipGraph",
    "")
){
    private val resources by option("--resources", help = UtilCLI.helpResourcesOpt)
        .file(mustExist = true, canBeDir = true, canBeFile = false)
        .required()
    private val numberOfTopCommits by option("--number-of-max-commits", help = "")
        .int()
        .default(Int.MAX_VALUE)
    private val numberOfNeighborsOfTopCommits by option("--number-of-neighbors-of-max-commits", help = "")
        .int()
        .default(5)

    override fun run() {
        val (nodes, edges) = createEdges()
        val graph = WeightedEdgesGraphHTML(nodes, edges)
        graph.draw(name, File("$name.html"))
    }

    fun getWeight(value: Double): Double {
        return value.pow(2)
    }

    private fun edgeColor(
        weight: Float,
        quantile1: String = "#0569E1",
        quantile2: String = "#5ba869",
        quantile3: String = "#FCAA05",
        quantile4: String = "#EE5503"
    ): String {
        if (weight <= 0.25f) return quantile1
        if (weight <= 0.5f) return quantile2
        if (weight <= 0.75f) return quantile3
        return quantile4
    }

    private fun createEdges(): Pair<Set<NodeInfo>, Set<EdgeInfo>> {
        val json = File(resources, ProjectConfig.DEVELOPER_KNOWLEDGE).readText()
        val adjacencyMap = Json.decodeFromString<HashMap<Int, HashMap<Int, Float>>>(json)

        val edges = mutableSetOf<EdgeInfo>()
        val nodes = mutableSetOf<NodeInfo>()

        val compareWeight: Comparator<Pair<Int, Float>> = compareBy { it.second }
        var n = 0
        var id = 0
        val files = HashMap<Int, Int>()
        for (entry in adjacencyMap) {
            if (n > numberOfNeighborsOfTopCommits) break
            val userIdReal = entry.key
            val userId = id
            id++

            nodes.add(NodeInfo(userId.toString(), "user_$userIdReal", shape = "square", background = "red", textColor = "black"))

            val priorityQueue = PriorityQueue(compareWeight)
            for (entry2 in entry.value) {
                val fileId = entry2.key
                val weight = entry2.value
                priorityQueue.add(fileId to weight)
                if (priorityQueue.size > numberOfTopCommits) priorityQueue.remove()
            }

            for (adj in priorityQueue) {
                val to = adj.first
                val weight = adj.second

                if (!files.containsKey(to)) {
                    files[to] = id
                    nodes.add(NodeInfo(id.toString(), "file_$to", textColor = "black"))
                    id++
                }

                edges.add(
                    EdgeInfo(
                    "$userId",
                    "${files[to]}",
                    color = edgeColor(weight),
                    value = getWeight(weight.toDouble()).toString()))

            }
            n++

        }
        return nodes to edges
    }
}

fun main(args: Array<String>) =
    CLI().subcommands(FilesOwnershipGraphCLI()).main(arrayOf("FilesOwnershipGraph", "--name", "testFilesOwnership", "--resources", "./resources", "--number-of-max-commits", "50"))

//fun main() {
//    val miner = FilesOwnershipMiner(FileRepository("../repos/react/.git"))
//    miner.run()
//    miner.saveToJson(File("./resources"))
//
//}
