package util

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

abstract class Mapper(private val entityToIdFileName: String, private val idToEntityFileName: String) {
    companion object {
        fun saveAll(resourceDirectory: File) {
            UserMapper.saveToJson(resourceDirectory)
            FileMapper.saveToJson(resourceDirectory)
            CommitMapper.saveToJson(resourceDirectory)
        }
    }

    private val entityToId = ConcurrentHashMap<String, Int>()
    private val idToEntity = ConcurrentHashMap<Int, String>()
    private var lastId = AtomicInteger(-1)

    fun add(value: String): Int {
//        TODO: find better solution
        val currId: Int
        synchronized(entityToId) {
            val id = entityToId[value]
            if (id != null) return id
            currId = lastId.incrementAndGet()
            entityToId[value] = currId
        }
        idToEntity[currId] = value

        return currId
    }

    fun saveToJson(resourceDirectory: File) {
        UtilFunctions.saveToJson(File(resourceDirectory, entityToIdFileName), entityToId.toMap())
        UtilFunctions.saveToJson(File(resourceDirectory, idToEntityFileName), idToEntity.toMap())
    }
}