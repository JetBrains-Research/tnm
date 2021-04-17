package util.mappers

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

// TODO: make generic, serialize error with generic Only KClass supported as classifier, got V
abstract class Mapper {

    protected val entityToId = ConcurrentHashMap<String, Int>()
    protected val idToEntity = ConcurrentHashMap<Int, String>()

    private var lastId = AtomicInteger(-1)

    fun get(): Map<String, Int> {
        return entityToId
    }

    fun add(value: String): Int {
        val currId = entityToId.computeIfAbsent(value) { lastId.incrementAndGet() }
        idToEntity[currId] = value
        return currId
    }
}
