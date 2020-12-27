package calculations

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.stat.StatUtils
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import util.ProjectConfig
import util.UtilFunctions
import java.io.File
import java.util.*
import kotlin.math.max

class CalcPageRank {
    fun run_Nd4j(resourceDirectory: File, alpha: Float = 0.85f) {
        val jsonCommitsMapper = File(resourceDirectory, ProjectConfig.COMMIT_ID).readText()
        val commitsMap = Json.decodeFromString<HashMap<String, Int>>(jsonCommitsMapper)
        val size = commitsMap.size

        val commitsGraphFile = File(resourceDirectory, ProjectConfig.COMMITS_GRAPH)

        val H = UtilFunctions.loadGraph_Nd4j(commitsGraphFile, size)
        val A = loadMatrixA_Nd4j(commitsGraphFile, size)
        val ones = Nd4j.ones(size, size)
        val G = H.muli(alpha).addi(A.muli(alpha)).addi(ones.muli((1 - alpha) / size))

        var I = Nd4j.zeros(size, 1)
        I.put(0, 0, 1F)


        // must be between 50-100 iterations
        for (i in 0 until 60) {
            I = G.mmul(I)
        }

        // TODO: do we really need?
        UtilFunctions.normalizeMax(I)
        UtilFunctions.saveToJson(File(resourceDirectory, ProjectConfig.PAGERANK_MATRIX), I.toFloatMatrix())
    }

    fun run(resourceDirectory: File, alpha: Double = 0.85) {
        val jsonCommitsMapper = File(resourceDirectory, ProjectConfig.COMMIT_ID).readText()
        val commitsMap = Json.decodeFromString<HashMap<String, Int>>(jsonCommitsMapper)
        val size = commitsMap.size

        val commitsGraphFile = File(resourceDirectory, ProjectConfig.COMMITS_GRAPH)
        val H = MatrixUtils.createRealMatrix(UtilFunctions.loadGraph(commitsGraphFile, size))
        val A = loadMatrixA(commitsGraphFile, size)
        val ones = MatrixUtils.createRealMatrix(Array(size) { DoubleArray(size) { 1.0 } })
        val G = H.scalarMultiply(alpha).add(A.scalarMultiply(alpha)).add(ones.scalarMultiply((1 - alpha) / size))


        var I = MatrixUtils.createRealMatrix(Array(size) { DoubleArray(1) { 0.0 } })
        I.setEntry(0, 0, 1.0)


        // must be between 50-100 iterations
        for (i in 0 until 60) {
            I = G.multiply(I)
        }

        I = I.scalarMultiply(1.0 / findMax(I))
        UtilFunctions.saveToJson(File(resourceDirectory, "${ProjectConfig.PAGERANK_MATRIX}_apache"), I.data)
    }

    // TODO: find set to 0
    private fun loadMatrixA_Nd4j(file: File, size: Int): INDArray {
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

    private fun loadMatrixA(file: File, size: Int): RealMatrix {
        val coefficient = 1.0 / size
        val result = MatrixUtils.createRealMatrix(Array(size) { DoubleArray(size) { coefficient } })
        val map = Json.decodeFromString<HashMap<Int, HashSet<Int>>>(file.readText())
        for (entry in map) {
            val nodeFrom = entry.key
            if (entry.value.size != 0) {
                result.setColumn(nodeFrom, DoubleArray(size) { 0.0 })
            }
        }
        return result
    }

    private fun findMax(matrix: RealMatrix): Double {
        var init = true
        var result = 0.0
        for (row in matrix.data) {
            if (init) {
                result = StatUtils.max(row)
                init = false
            } else {
                result = max(result, StatUtils.max(row))
            }
        }
        return result
    }

}

fun main() {
    val calc = CalcPageRank()
//    calc.run_Nd4j(File(ProjectConfig.RESOURCES_PATH))
    calc.run(File(ProjectConfig.RESOURCES_PATH))
}
