package util

/**
 * This object maps users to unique id.
 *
 */
object UserMapper : Mapper(ProjectConfig.USER_ID, ProjectConfig.ID_USER)
