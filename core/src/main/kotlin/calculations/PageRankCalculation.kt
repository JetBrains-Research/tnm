package calculations

import org.jgrapht.alg.scoring.PageRank
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.builder.GraphBuilder
import org.jgrapht.graph.builder.GraphTypeBuilder

object PageRankCalculation {
    const val DEFAULT_ALPHA = 0.85

    fun run(
        commitsGraph: Map<Int, Set<Int>>,
        alpha: Double = DEFAULT_ALPHA
    ): Map<Int, Float> {

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
        return PageRank(finalGraph, alpha).scores.mapValues { it.value.toFloat() }
    }

}
