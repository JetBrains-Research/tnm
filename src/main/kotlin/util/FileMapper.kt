package util

import com.google.gson.Gson
import java.io.File

object FileMapper: Mapper {
    override val gson: Gson = Gson()

    val fileToId = HashMap<String, Int>()

    var lastFileId = 0
    private set

    override fun add(value: String): Int {
        val id = fileToId[value]
        if (id != null) return id

        fileToId[value] = lastFileId
        lastFileId++
        return lastFileId - 1
    }

    override fun saveToJson() {
        File("./resources/fileToId").writeText(gson.toJson(fileToId))
    }
}