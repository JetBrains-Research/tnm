package util

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * This object maps users to unique id.
 *
 */
object UserMapper : Mapper {
    val userToId = ConcurrentHashMap<String, Int>()
    val idToUser = ConcurrentHashMap<Int, String>()

    val lastUserId = AtomicInteger(-1)

    override fun add(value: String): Int {
        val id = userToId[value]
        if (id != null) return id

        val currId = lastUserId.incrementAndGet()

        userToId[value] = currId
        idToUser[currId] = value
        return currId
    }

    override fun saveToJson(resourceDirectory: File) {
        UtilFunctions.saveToJson(File(resourceDirectory, ProjectConfig.USER_ID), userToId.toMap())
        UtilFunctions.saveToJson(File(resourceDirectory, ProjectConfig.ID_USER), idToUser.toMap())
    }
}