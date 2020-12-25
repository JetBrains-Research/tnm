package gitMiners

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.revwalk.RevCommit
import util.FileMapper
import util.ProjectConfig
import util.UserMapper
import util.UtilFunctions
import java.io.File

class ChangedFilesMiner(
    override val repository: FileRepository,
    override val neededBranches: Set<String> = ProjectConfig.neededBranches
) : GitMiner() {
    override val git = Git(repository)
    override val reader: ObjectReader = repository.newObjectReader()

    private val userFilesIds = hashMapOf<Int, MutableSet<Int>>()

    // TODO: add FilesChanges[fileId] = Set(commit1, ...)
    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        val userEmail = currCommit.authorIdent.emailAddress
        val userId = UserMapper.add(userEmail)
        val changedFiles = UtilGitMiner.getChangedFiles(currCommit, prevCommit, reader, git)

        for (fileId in changedFiles) {
            userFilesIds.computeIfAbsent(userId) { mutableSetOf() }.add(fileId)
        }
    }

    override fun saveToJson(resourceDirectory: File) {
        UtilFunctions.saveToJson(File(resourceDirectory, ProjectConfig.USER_FILES_IDS), userFilesIds)
    }
}

fun main() {
    val parseChangedFiles = ChangedFilesMiner(ProjectConfig.repository)
    parseChangedFiles.run()
    FileMapper.saveToJson(File(ProjectConfig.RESOURCES_PATH))
    UserMapper.saveToJson(File(ProjectConfig.RESOURCES_PATH))
}