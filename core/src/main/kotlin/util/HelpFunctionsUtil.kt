package util

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.lib.RepositoryCache
import org.eclipse.jgit.util.FS
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import kotlin.math.log2

object HelpFunctionsUtil {
    val json = Json { encodeDefaults = true }

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
        val jsonString = json.encodeToString(data)
        file.writeText(jsonString)
    }

    inline fun <reified T> saveToJson(file: File, data: T, serializer: SerializationStrategy<T>) {
        createParentFolder(file)
        val jsonString = json.encodeToString(serializer, data)
        file.writeText(jsonString)
    }

    fun deleteDir(directory: File) {
        if (directory.exists() && directory.isDirectory) {
            try {
                FileUtils.deleteDirectory(directory)
            } catch (e: Exception) {
                println("Got error while cleaning directory $directory: $e")
            }
        }
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
                    .computeIfAbsent(newKey1) { HashMap() }[newKey2] = value
            }
        }

        return result
    }

    fun convertMapToArray(map: Map<Int, Map<Int, Int>>, rows: Int, columns: Int): Array<FloatArray> {
        val result = Array(rows) { FloatArray(columns) }
        for ((x, innerMap) in map) {
            for ((y, value) in innerMap) {
                result[x][y] = value.toFloat()
            }
        }
        return result
    }

    fun convertMapTo1dArray(map: Map<Int, Map<Int, Int>>, rows: Int, columns: Int): FloatArray {
        val result = FloatArray(rows * columns) { 0f }
        for ((x, innerMap) in map) {
            for ((y, value) in innerMap) {
                val idx = x * columns + y
                result[idx] = value.toFloat()
            }
        }
        return result
    }

    fun convertLowerTriangleMapToArray(map: Map<Int, Map<Int, Int>>, rows: Int, columns: Int): Array<FloatArray> {
        val result = Array(rows) { FloatArray(columns) }
        for ((x, innerMap) in map) {
            for ((y, value) in innerMap) {
                result[x][y] = value.toFloat()
                result[y][x] = value.toFloat()
            }
        }
        return result
    }

    fun convertLowerTriangleMapTo1dArray(map: Map<Int, Map<Int, Int>>, rows: Int, columns: Int): FloatArray {
        val result = FloatArray(rows * columns) { 0f }
        for ((x, innerMap) in map) {
            for ((y, value) in innerMap) {
                val idx1 = x * columns + y
                result[idx1] = value.toFloat()

                val idx2 = y * columns + x
                result[idx2] = value.toFloat()
            }
        }
        return result
    }

    fun entropy(distribution: Collection<Int>): Double {
        var result = 0.0
        for (value in distribution) {
            result += value * log2(value.toDouble())
        }
        return -result
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
