package util

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler
import org.nd4j.linalg.factory.Nd4j
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object UtilFunctions {
    // TODO: try catch logic for encode and file
    inline fun <reified T> saveToJson(file: File, data: T) {
        val jsonString = Json.encodeToString(data)
        file.writeText(jsonString)
    }

    fun loadArray(file: File, rows: Int, columns: Int): INDArray {
        val result = Array(rows) { FloatArray(columns) }
        val adjacencyMap = Json.decodeFromString<HashMap<Int, HashMap<Int, Int>>>(file.readText())
        for ((x, innerMap) in adjacencyMap) {
            for ((y, value) in innerMap) {
                result[x][y] = value.toFloat()
            }
        }
        return Nd4j.create(result)
    }

    fun loadGraph(file: File, size: Int): INDArray {
        val result = Array(size) { FloatArray(size) }
        val adjacencyMap = Json.decodeFromString<HashMap<Int, HashSet<Int>>>(file.readText())
        for (entry in adjacencyMap) {
            val nodeFrom = entry.key
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

    fun convertConcurrentMapOfConcurrentMapsInt(map: ConcurrentHashMap<Int, ConcurrentHashMap<Int, Int>>): Map<Int, Map<Int, Int>> {
        val newMap = HashMap<Int, Map<Int, Int>>()
        for (entry in map.entries) {
            newMap[entry.key] = entry.value
        }
        return newMap
    }
}
