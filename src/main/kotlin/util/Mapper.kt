package util

import kotlinx.serialization.builtins.serializer
import util.serialization.ConcurrentHashMapSerializer
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

// TODO: make generic, serialize error with generic Only KClass supported as classifier, got V
abstract class Mapper(private val entityToIdFileName: String, private val idToEntityFileName: String) {
    private val entityToId = ConcurrentHashMap<String, Int>()
    private val idToEntity = ConcurrentHashMap<Int, String>()
    private var lastId = AtomicInteger(-1)


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
