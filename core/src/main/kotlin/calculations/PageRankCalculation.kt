package calculations

import org.jgrapht.alg.scoring.PageRank
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.builder.GraphBuilder
import org.jgrapht.graph.builder.GraphTypeBuilder

class PageRankCalculation(
    private val commitsGraph: Map<Int, Set<Int>>,
    private val alpha: Double = DEFAULT_ALPHA
) : Calculation {
    companion object {
        const val DEFAULT_ALPHA = 0.85
    }

    private var _pageRank: Map<Int, Float>? = null
    val pageRank: Map<Int, Float>
        get() = _pageRank ?: HashMap()

    override fun run() {
        val graph = GraphTypeBuilder.directed<Int, DefaultEdge>()
            .allowingMultipleEdges(false)
            .allowingSelfLoops(true)
            .weighted(true)
            .edgeClass(DefaultEdge::class.java)
            .buildGraph()

        val graphBuilder = GraphBuilder(graph)
        for (entry in commitsGraph) {
            val nodeFrom = entry.key
            graphBuilder.addVertex(nodeFrom)
            for (nodeTo in entry.value) {
                graphBuilder.addEdge(nodeFrom, nodeTo, 1.0)
            }
        }

        val finalGraph = graphBuilder.build()
        _pageRank = PageRank(finalGraph, alpha).scores.mapValues { it.value.toFloat() }
    }

}
