package util

import java.io.File


/**
 * This object maps files to unique id.
 *
 */
object FileMapper : Mapper {
    private val fileToId = HashMap<String, Int>()
    private var lastFileId = 0

    override fun add(value: String): Int {
        val id = fileToId[value]
        if (id != null) return id

        fileToId[value] = lastFileId
        lastFileId++
        return lastFileId - 1
    }

    override fun saveToJson(resourceDirectory: File) {
        UtilFunctions.saveToJson(File(resourceDirectory, ProjectConfig.FILE_ID), fileToId)
    }
}