package util.mappers


/**
 * This object maps commits to unique id.
 *
 */
class CommitMapper : Mapper() {
    val commitToId: Map<String, Int>
        get() = entityToId

    val idToCommit: Map<Int, String>
        get() = idToEntity
}
