package util

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


/**
 * This object maps commits to unique id.
 *
 */
object CommitMapper : Mapper {
    private val commitToId = ConcurrentHashMap<String, Int>()
    private val idToCommit = ConcurrentHashMap<Int, String>()
    private var lastCommitId = AtomicInteger(-1)


    override fun add(value: String): Int {
        val id = commitToId[value]
        if (id != null) return id

        val currId = lastCommitId.incrementAndGet()
        commitToId[value] = currId
        idToCommit[currId] = value

        return currId
    }

    override fun saveToJson(resourceDirectory: File) {
        UtilFunctions.saveToJson(File(resourceDirectory, ProjectConfig.COMMIT_ID), commitToId.toMap())
        UtilFunctions.saveToJson(File(resourceDirectory, ProjectConfig.ID_COMMIT), idToCommit.toMap())
    }
}