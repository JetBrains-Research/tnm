package util

import com.google.gson.Gson
import java.io.File

object FileMapper: Mapper {
    override val gson: Gson = Gson()

    val fileToId = HashMap<String, Int>()

    var lastFileId = 0
    private set

    override fun add(value: String) {
        if (!fileToId.contains(value)) {
            fileToId[value] = lastFileId
            lastFileId++
        }
    }

    override fun saveToJson() {
        File("./resources/fileToId").writeText(gson.toJson(fileToId))
    }
}