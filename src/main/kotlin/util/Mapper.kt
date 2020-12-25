package util

import java.io.File

interface Mapper {
    fun add(value: String): Int
    fun saveToJson(resourceDirectory: File)
}