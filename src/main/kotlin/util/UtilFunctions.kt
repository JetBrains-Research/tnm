package util

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler
import org.nd4j.linalg.factory.Nd4j
import java.io.File
import kotlin.math.log2

object UtilFunctions {
    fun createParentFolder(file: File) {
        val folder = File(file.parent)
        folder.mkdirs()
    }

    // TODO: try catch logic for encode and file
    inline fun <reified T> saveToJson(file: File, data: T) {
        createParentFolder(file)
        val jsonString = Json.encodeToString(data)
        file.writeText(jsonString)
    }

    inline fun <reified T> saveToJson(file: File, data: T, serializer: SerializationStrategy<T>) {
        createParentFolder(file)
        val jsonString = Json.encodeToString(serializer, data)
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

    fun loadArray(
        file: File,
        rows: Int,
        columns: Int,
        idToEntity: Map<Int, String>,
        entityToId: Map<String, Int>
    ): INDArray {
        val result = Array(rows) { FloatArray(columns) }
        val adjacencyMap = Json.decodeFromString<HashMap<Int, HashMap<Int, Int>>>(file.readText())
        for ((x, innerMap) in adjacencyMap) {
            for ((y, value) in innerMap) {
                val neededX = entityToId[idToEntity[x]!!]!!
                val neededY = entityToId[idToEntity[y]!!]!!
                result[neededX][neededY] = value.toFloat()
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

    fun entropy(distribution: Collection<Int>): Double {
        var result = 0.0
        for (value in distribution) {
            result += value * log2(value.toDouble())
        }
        return -result
    }

    fun levenshtein(str1: String, str2: String): Int {
        val Di_1 = IntArray(str2.length + 1)
        val Di = IntArray(str2.length + 1)
        for (j in 0..str2.length) {
            Di[j] = j // (i == 0)
        }
        for (i in 1..str1.length) {
            System.arraycopy(Di, 0, Di_1, 0, Di_1.size)
            Di[0] = i // (j == 0)
            for (j in 1..str2.length) {
                val cost = if (str1[i - 1] != str2[j - 1]) 1 else 0
                Di[j] = (Di_1[j] + 1)
                    .coerceAtMost(Di[j - 1] + 1)
                    .coerceAtMost(Di_1[j - 1] + cost)
            }
        }
        return Di[Di.size - 1]
    }

}
