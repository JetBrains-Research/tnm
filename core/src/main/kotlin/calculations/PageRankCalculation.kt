package calculations

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import util.UtilFunctions

class PageRankCalculation(
    private val commitsGraph: Map<Int, Set<Int>>,
    private val numOfCommits: Int,
    private val alpha: Float = DEFAULT_ALPHA
) : Calculation {
    companion object {
        const val DEFAULT_ALPHA = 0.85f
    }

    private var _pageRank: INDArray? = null
    val pageRank: Array<out FloatArray>
        get() = _pageRank?.toFloatMatrix() ?: emptyArray()

    override fun run() {
        val H = UtilFunctions.loadGraph(commitsGraph, numOfCommits)
        val A = loadMatrixA(commitsGraph, numOfCommits)
        val ones = Nd4j.ones(numOfCommits, numOfCommits)
        val G = H.muli(alpha).addi(A.muli(alpha)).addi(ones.muli((1 - alpha) / numOfCommits))

        var I = Nd4j.zeros(numOfCommits, 1)
        I.put(0, 0, 1F)

        // must be between 50-100 iterations
        for (i in 0 until 75) {
            I = G.mmul(I)
        }

        val maxValue = I.max().toFloatVector()[0]
        if (maxValue > 1.0f) {
            throw Exception("PageRankCalculation according to algorithm of Suzuki et al. is not supported for this repository.")
        }

        _pageRank = I
    }

    private fun loadMatrixA(adjacencyMap: Map<Int, Set<Int>>, size: Int): INDArray {
        val coefficient = 1F / size
        val result = Nd4j.create(Array(size) { FloatArray(size) { coefficient } })
        for (entry in adjacencyMap) {
            val nodeFrom = entry.key
            if (entry.value.isNotEmpty()) {
                result.getColumn(nodeFrom.toLong()).addi(-coefficient)
            }
        }
        return result
    }

}
