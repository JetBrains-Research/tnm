package util

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler
import org.nd4j.linalg.factory.Nd4j
import java.io.File

object UtilFunctions {
    // TODO: try catch logic for encode and file
    inline fun <reified T> saveToJson(filePath: String, data: T) {
        val jsonString = Json.encodeToString(data)
        File(filePath).writeText(jsonString)
    }

    fun loadArray(file: File, rows: Int, columns: Int): INDArray {
        val result = Array(rows) { FloatArray(columns) }
        val map = Json.decodeFromString<HashMap<Int, HashMap<Int, Int>>>(file.readText())
        for ((x, innerMap) in map) {
            for ((y, value) in innerMap) {
                result[x][y] = value.toFloat()
            }
        }
        return Nd4j.create(result)
    }

    fun loadGraph(file: File, size: Int): INDArray {
        val result = Array(size) { FloatArray(size) }
        val map = Json.decodeFromString<HashMap<Int, HashSet<Int>>>(file.readText())
        for (entry in map) {
            val nodeFrom  = entry.key
            for (nodeTo in entry.value) {
                result[nodeFrom][nodeTo] = 1F
            }
        }

        return Nd4j.create(result)
    }

    fun normalizeMax(matrix: INDArray) {
        val scaler = NormalizerMinMaxScaler()
        scaler.setFeatureStats(Nd4j.create(1).add(matrix.min()), Nd4j.create(1).add(matrix.max()))
        scaler.transform(matrix)
    }
}