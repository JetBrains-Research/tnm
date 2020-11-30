package gitMiners

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.revwalk.RevCommit
import util.FileMapper
import util.ProjectConfig
import util.UserMapper
import util.UtilFunctions

class ChangedFilesMiner(override val repository: FileRepository) : GitMiner() {
    override val git = Git(repository)
    override val reader: ObjectReader = repository.newObjectReader()

    private val userFilesIds = hashMapOf<Int, MutableSet<Int>>()

    // TODO: add FilesChanges[fileId] = Set(commit1, ...)
    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        val userEmail = currCommit.authorIdent.emailAddress
        val userId = UserMapper.add(userEmail)
        val changedFiles = getChangedFiles(currCommit, prevCommit, reader, git)

        for (fileId in changedFiles) {
            userFilesIds.computeIfAbsent(userId) { mutableSetOf() }.add(fileId)
        }
    }

    override fun saveToJson() {
        UtilFunctions.saveToJson(ProjectConfig.USER_FILES_IDS_PATH, userFilesIds)
    }
}

fun main() {
    val parseChangedFiles = ChangedFilesMiner(ProjectConfig.repository)
    parseChangedFiles.run()
    FileMapper.saveToJson()
    UserMapper.saveToJson()
}