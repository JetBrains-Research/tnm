package util


/**
 * This object maps files to unique id.
 *
 */
class FileMapper : Mapper(ProjectConfig.FILE_ID, ProjectConfig.ID_FILE) {
    val fileToId : Map<String, Int>
        get() = entityToId

    val idToFile : Map<Int, String>
        get() = idToEntity
}
