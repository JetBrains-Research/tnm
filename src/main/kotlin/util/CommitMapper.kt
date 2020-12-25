package util

import java.io.File


/**
 * This object maps commits to unique id.
 *
 */
object CommitMapper : Mapper {
    private val commitToId = HashMap<String, Int>()
    private val idToCommit = HashMap<Int, String>()
    private var lastCommitId = 0


    override fun add(value: String): Int {
        val id = commitToId[value]
        if (id != null) return id

        commitToId[value] = lastCommitId
        idToCommit[lastCommitId] = value
        lastCommitId++
        return lastCommitId - 1
    }

    override fun saveToJson(resourceDirectory: File) {
        UtilFunctions.saveToJson(File(resourceDirectory, ProjectConfig.COMMIT_ID), commitToId)
        UtilFunctions.saveToJson(File(resourceDirectory, ProjectConfig.ID_COMMIT), idToCommit)
    }
}