package util.mappers


/**
 * This object maps files to unique id.
 *
 */
class FileMapper : Mapper() {
    val fileToId: Map<String, Int>
        get() = entityToId

    val idToFile: Map<Int, String>
        get() = idToEntity
}
