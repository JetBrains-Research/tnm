package util

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


/**
 * This object maps files to unique id.
 *
 */
object FileMapper : Mapper {
    private val fileToId = ConcurrentHashMap<String, Int>()
    private val idToFile = ConcurrentHashMap<Int, String>()
    private var lastFileId = AtomicInteger(-1)

    override fun add(value: String): Int {
        val id = fileToId[value]
        if (id != null) return id

        val currId = lastFileId.incrementAndGet()

        fileToId[value] = currId
        idToFile[currId] = value

        return currId
    }

    override fun saveToJson(resourceDirectory: File) {
        UtilFunctions.saveToJson(File(resourceDirectory, ProjectConfig.FILE_ID), fileToId.toMap())
        UtilFunctions.saveToJson(File(resourceDirectory, ProjectConfig.ID_FILE), idToFile.toMap())
    }
}