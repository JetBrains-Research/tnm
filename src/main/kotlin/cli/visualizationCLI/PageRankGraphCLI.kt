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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import util.ProjectConfig
import util.StorageN
import visualisation.EdgeInfo
import visualisation.NodeInfo
import visualisation.WeightedEdgesGraphHTML
import java.io.File
import java.util.*
import kotlin.collections.HashMap

class PageRankGraphCLI : VisualizationCLI(
    InfoCLI("PageRankGraph", "")
) {
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

    private fun getWeight(value: Float, maxValue: Float): Float {
        return (value / maxValue) * 300
    }

    // TODO: add file check
    // TODO: better logic
    // TODO:
    private fun createEdges(): Pair<Set<NodeInfo>, Set<EdgeInfo>> {
        val commitsGraphFile = File(resources, ProjectConfig.COMMITS_GRAPH)
        val pageRankFile = File(resources, ProjectConfig.PAGERANK_MATRIX)

        val adjacencyMap = Json.decodeFromString<HashMap<Int, HashSet<Int>>>(commitsGraphFile.readText())

        val pageRankValues = Json.decodeFromString<List<List<Float>>>(pageRankFile.readText())
        val nodesMap = HashMap<Int, NodeInfo>()

        val edges = mutableSetOf<EdgeInfo>()
        val nodes = mutableSetOf<NodeInfo>()

        val compareWeight: Comparator<Pair<Int, Float>> = compareBy { it.second }
        val topCommitsStorage = StorageN(numberOfTopCommits, compareWeight)

        pageRankValues.forEachIndexed { i, rank ->
            val pageRankValue = rank[0]
            val adj = adjacencyMap[i]
            if (adj!!.isNotEmpty()) {
                topCommitsStorage.add(i to pageRankValue)
            }
        }

        pageRankValues.forEachIndexed { i, rank ->
            val v = rank[0]
            val weight = getWeight(v, topCommitsStorage.high!!.second)
            nodesMap[i] = NodeInfo(i.toString(), size = weight.toString(), textColor = "black")
        }


        for (node in topCommitsStorage.get()) {
            val weight = getWeight(node.second, topCommitsStorage.high!!.second)
            nodesMap[node.first] = NodeInfo(
                node.first.toString(),
                size = weight.toString(),
                shape = "diamond",
                textColor = "black",
                background = "red"
            )
            nodes.add(nodesMap[node.first]!!)
        }


        val compareAdj: Comparator<Pair<Int, Float>> = compareBy { it.second }
        for (bF in topCommitsStorage.get()) {
            val from = bF.first
            val adjCommitsToTopStorage = StorageN(numberOfNeighborsOfTopCommits, compareAdj)

            val adjList = adjacencyMap[from] ?: continue
            for (adj in adjList) {
                adjCommitsToTopStorage.add(adj to nodesMap[adj]!!.size.toFloat())
            }

            for (node in adjCommitsToTopStorage.get()) {
                if (node.first == from) continue
                edges.add(
                    EdgeInfo(
                        from.toString(),
                        node.first.toString(),
                        color = "#000000",
                        value = (0.0001).toString()
                    )
                )
                nodes.add(nodesMap[node.first]!!)
            }
        }


        return nodes to edges
    }
}

fun main(args: Array<String>) =
    CLI().subcommands(PageRankGraphCLI()).main(arrayOf("PageRankGraph", "--name", "testPage", "--resources", "./resources", "--number-of-max-commits", "50"))

//fun main() {
//    val miner = CommitInfluenceGraphMiner(FileRepository("../repos/react/.git"))
//    miner.run()
//    miner.saveToJson(File("./resources"))
//
//    val calc = PageRankCalculation(File("./resources"))
//    calc.run()
//    calc.saveToJson(File("./resources"))
//}
