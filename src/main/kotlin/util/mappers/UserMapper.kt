package util.mappers

/**
 * This object maps users to unique id.
 *
 */
class UserMapper : Mapper() {
    val userToId: Map<String, Int>
        get() = entityToId

    val idToUser: Map<Int, String>
        get() = idToEntity
}
