package util

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object UtilFunctions {
    // TODO: try catch logic for encode and file
    inline fun <reified T> saveToJson(filePath: String, data: T) {
        val jsonString = Json.encodeToString(data)
        File(filePath).writeText(jsonString)
    }
}