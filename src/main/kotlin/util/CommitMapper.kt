package util


/**
 * This object maps commits to unique id.
 *
 */
class CommitMapper : Mapper(ProjectConfig.COMMIT_ID, ProjectConfig.ID_COMMIT) {
    val commitToId : Map<String, Int>
        get() = entityToId

    val idToCommit : Map<Int, String>
        get() = idToEntity
}
