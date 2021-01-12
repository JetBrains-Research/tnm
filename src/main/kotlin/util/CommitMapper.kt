package util


/**
 * This object maps commits to unique id.
 *
 */
object CommitMapper : Mapper(ProjectConfig.COMMIT_ID, ProjectConfig.ID_COMMIT)
