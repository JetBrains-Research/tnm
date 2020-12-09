package calculations

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import util.ProjectConfig
import util.UtilFunctions
import java.io.File

class CalcPageRank {
    fun run(alpha: Float = 0.85f) {
        val jsonCommitsMapper = File(ProjectConfig.COMMIT_ID_PATH).readText()
        val commitsMap = Json.decodeFromString<HashMap<String, Int>>(jsonCommitsMapper)
        val size = commitsMap.size

        val commitsGraphFile = File(ProjectConfig.COMMITS_GRAPH_PATH)

        val H = UtilFunctions.loadGraph(commitsGraphFile, size)
        val A = loadMatrixA(commitsGraphFile, size)
        val ones = Nd4j.ones(size, size)
        val G = H.muli(alpha).addi(A.muli(alpha)).addi(ones.muli( (1 - alpha) / size ))

        var I =  Nd4j.zeros(size, 1)
        I.put(0,0,1F)


        // must be between 50-100 iterations
        for (i in 0 until 60) {
            I = G.mmul(I)
        }

        // TODO: do we really need?
        UtilFunctions.normalizeMax(I)
        UtilFunctions.saveToJson(ProjectConfig.PAGERANK_MATRIX_PATH, I.toFloatMatrix())

    }

    // TODO: find set to 0
    private fun loadMatrixA(file: File, size: Int): INDArray {
        val coefficient = 1F / size
        val result = Nd4j.create(Array(size) { FloatArray(size) { coefficient } })
        val map = Json.decodeFromString<HashMap<Int, HashSet<Int>>>(file.readText())
        for (entry in map) {
            val nodeFrom  = entry.key
            if (entry.value.size != 0) {
                result.getColumn(nodeFrom.toLong()).addi(-coefficient)
            }
        }
        return result
    }
}

fun main() {
    val calc = CalcPageRank()
    calc.run()
}