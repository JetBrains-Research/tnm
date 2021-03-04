package util

import kotlinx.serialization.builtins.serializer
import util.serialization.ConcurrentHashMapSerializer
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

// TODO: make generic, serialize error with generic Only KClass supported as classifier, got V
abstract class Mapper(private val entityToIdFileName: String, private val idToEntityFileName: String) {
    companion object {
        fun saveAll(resourceDirectory: File) {
            UserMapper.saveToJson(resourceDirectory)
            FileMapper.saveToJson(resourceDirectory)
            CommitMapper.saveToJson(resourceDirectory)
        }

        const val EMPTY_VALUE = ""
        const val EMPTY_VALUE_ID = -1
    }

    private val entityToId = ConcurrentHashMap<String, Int>()
    private val idToEntity = ConcurrentHashMap<Int, String>()
    private var lastId = AtomicInteger(-1)

    init {
        entityToId[EMPTY_VALUE] = EMPTY_VALUE_ID
        idToEntity[EMPTY_VALUE_ID] = EMPTY_VALUE
    }

    fun add(value: String): Int {
        val currId = entityToId.computeIfAbsent(value) { lastId.incrementAndGet() }
        idToEntity[currId] = value
        return currId
    }

    fun saveToJson(resourceDirectory: File) {
        val serializerEntityToId = ConcurrentHashMapSerializer(String.serializer(), Int.serializer())
        UtilFunctions.saveToJson(File(resourceDirectory, entityToIdFileName), entityToId, serializerEntityToId)

        val serializerIdToEntity = ConcurrentHashMapSerializer(Int.serializer(), String.serializer())
        UtilFunctions.saveToJson(File(resourceDirectory, idToEntityFileName), idToEntity, serializerIdToEntity)
    }
}
