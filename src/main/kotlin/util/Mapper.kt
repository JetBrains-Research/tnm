package util

import java.io.File

interface Mapper {
    companion object {
        fun saveAll(resourceDirectory: File) {
            UserMapper.saveToJson(resourceDirectory)
            FileMapper.saveToJson(resourceDirectory)
            CommitMapper.saveToJson(resourceDirectory)
        }
    }

    fun add(value: String): Int
    fun saveToJson(resourceDirectory: File)
}