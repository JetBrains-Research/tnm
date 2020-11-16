package util

import com.google.gson.Gson
import java.io.File

object CommitMapper : Mapper {
    override val gson: Gson = Gson()
    val commitToId = HashMap<String, Int>()
    val idToCommit = HashMap<Int, String>()
    var lastCommitId = 0

    override fun add(value: String):Int {
        val id  = commitToId[value]
        if (id != null) return id

        commitToId[value] = lastCommitId
        idToCommit[lastCommitId] = value
        lastCommitId++
        return lastCommitId - 1
    }

    override fun saveToJson() {
        File("./resources/commitToId").writeText(gson.toJson(commitToId))
        File("./resources/idToCommit").writeText(gson.toJson(idToCommit))
    }
}