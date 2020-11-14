package util

import com.google.gson.Gson

interface Mapper {
    val gson: Gson

    fun add(value: String)
    fun saveToJson()
}