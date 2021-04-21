package util

import dataProcessor.DataProcessorMapped
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.lib.RepositoryCache
import org.eclipse.jgit.util.FS
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler
import org.nd4j.linalg.factory.Nd4j
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import kotlin.math.log2

object UtilFunctions {

    fun isGitRepository(directory: File): Boolean {
        return RepositoryCache.FileKey.isGitRepository(directory, FS.DETECTED)
    }

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

    fun <T> changeKeysInMapOfMaps(
        map: Map<Int, Map<Int, T>>,
        keyToValue1: Map<Int, String>, valueToNewKey1: Map<String, Int>,
        keyToValue2: Map<Int, String>, valueToNewKey2: Map<String, Int>,
    ): Map<Int, Map<Int, T>> {

        val result = HashMap<Int, HashMap<Int, T>>()

        for (entry1 in map) {
            val key1 = entry1.key
            val newKey1 = valueToNewKey1[keyToValue1[key1]!!]!!
            for (entry2 in entry1.value) {
                val key2 = entry2.key
                val value = entry2.value

                val newKey2 = valueToNewKey2[keyToValue2[key2]!!]!!

                result
                    .computeIfAbsent(newKey1) {HashMap()} [newKey2] = value
            }
        }

        return result
    }

    fun convertMapToArray(map:Map<Int, Map<Int, Int>>, rows: Int, columns: Int): INDArray {
        val result = Array(rows) { FloatArray(columns) }
        for ((x, innerMap) in map) {
            for ((y, value) in innerMap) {
                result[x][y] = value.toFloat()
            }
        }
        return Nd4j.create(result)
    }

    fun loadGraph(adjacencyMap: Map<Int, Set<Int>>, size: Int): INDArray {
        val result = Array(size) { FloatArray(size) }
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

    fun runInThreadPoolWithExceptionHandle(threadPool: ExecutorService, tasks: List<Runnable>) {
        val futures = mutableListOf<Future<*>>()
        for (task in tasks) {
            futures.add(threadPool.submit(task))
        }

        for (future in futures) {
            try {
                future.get()
            } catch (e: Exception) {
                threadPool.shutdownNow()
                throw e
            }
        }
    }
}
