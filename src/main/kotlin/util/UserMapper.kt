package util

import com.google.gson.Gson
import java.io.File

object UserMapper : Mapper {
    override val gson: Gson = Gson()

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
        File("./resources/userToId").writeText(gson.toJson(userToId))
        File("./resources/idToUser").writeText(gson.toJson(idToUser))
    }
}