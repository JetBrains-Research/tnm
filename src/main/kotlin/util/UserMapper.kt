package util

import java.io.File

/**
 * This object maps users to unique id.
 *
 */
object UserMapper : Mapper {
    val userToId = HashMap<String, Int>()
    val idToUser = HashMap<Int, String>()

    var lastUserId = 0
        private set

    override fun add(value: String): Int {
        val id = userToId[value]
        if (id != null) return id

        userToId[value] = lastUserId
        idToUser[lastUserId] = value
        lastUserId++
        return lastUserId - 1
    }

    override fun saveToJson(resourceDirectory: File) {
        UtilFunctions.saveToJson(File(resourceDirectory, ProjectConfig.USER_ID), userToId)
        UtilFunctions.saveToJson(File(resourceDirectory, ProjectConfig.ID_USER), idToUser)
    }
}