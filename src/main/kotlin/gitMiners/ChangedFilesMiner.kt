package gitMiners

import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevCommit
import util.Mapper
import util.ProjectConfig
import util.UserMapper
import util.UtilFunctions
import java.io.File

class ChangedFilesMiner(
    repository: FileRepository,
    neededBranches: Set<String> = ProjectConfig.neededBranches
) : GitMiner(repository, neededBranches) {

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
        Mapper.saveAll(resourceDirectory)
    }
}
