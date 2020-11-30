package util


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

    override fun saveToJson() {
        UtilFunctions.saveToJson(ProjectConfig.COMMIT_ID_PATH, commitToId)
        UtilFunctions.saveToJson(ProjectConfig.ID_COMMIT_PATH, idToCommit)
    }
}