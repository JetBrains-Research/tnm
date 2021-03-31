package calculations

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import util.ProjectConfig
import util.UtilFunctions
import java.io.File

class PageRankCalculation(resourceDirectory: File, private val alpha: Float = 0.85f) : Calculation {
    var result: INDArray? = null
        private set

    private val size: Int
    private val commitsGraphFile = File(resourceDirectory, ProjectConfig.COMMITS_GRAPH)

    init {
        val jsonCommitsMapper = File(resourceDirectory, ProjectConfig.COMMIT_ID).readText()
        val commitsMap = Json.decodeFromString<HashMap<String, Int>>(jsonCommitsMapper)
        size = commitsMap.size
    }

    override fun run() {
        val H = UtilFunctions.loadGraph(commitsGraphFile, size)
        val A = loadMatrixA(commitsGraphFile, size)
        val ones = Nd4j.ones(size, size)
        val G = H.muli(alpha).addi(A.muli(alpha)).addi(ones.muli((1 - alpha) / size))

        var I = Nd4j.zeros(size, 1)
        I.put(0, 0, 1F)

        // must be between 50-100 iterations
        for (i in 0 until 75) {
            I = G.mmul(I)
        }

        val maxValue = I.max().toFloatVector()[0]
        if (maxValue > 1.0f) {
            throw Exception("PageRankCalculation according to algorithm of Suzuki et al. is not supported for this repository.")
        }

        result = I
    }

    override fun saveToJson(resourceDirectory: File) {
        result?.let {
            UtilFunctions.saveToJson(
                File(resourceDirectory, ProjectConfig.PAGERANK_MATRIX),
                it.toFloatMatrix()
            )
        }
    }

    private fun loadMatrixA(file: File, size: Int): INDArray {
        val coefficient = 1F / size
        val result = Nd4j.create(Array(size) { FloatArray(size) { coefficient } })
        val map = Json.decodeFromString<HashMap<Int, HashSet<Int>>>(file.readText())
        for (entry in map) {
            val nodeFrom = entry.key
            if (entry.value.size != 0) {
                result.getColumn(nodeFrom.toLong()).addi(-coefficient)
            }
        }
        return result
    }

}
