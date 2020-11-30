package util

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

    override fun saveToJson() {
        UtilFunctions.saveToJson(ProjectConfig.USER_ID_PATH, userToId)
        UtilFunctions.saveToJson(ProjectConfig.ID_USER_PATH, idToUser)
    }
}