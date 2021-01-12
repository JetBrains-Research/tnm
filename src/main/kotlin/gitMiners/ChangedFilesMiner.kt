package gitMiners

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevCommit
import util.Mapper
import util.ProjectConfig
import util.UserMapper
import util.UtilFunctions
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet

class ChangedFilesMiner(
    repository: FileRepository,
    neededBranches: Set<String> = ProjectConfig.neededBranches,
    numThreads: Int = ProjectConfig.numThreads
) : GitMiner(repository, neededBranches, numThreads = numThreads) {

    private val userFilesIds = ConcurrentHashMap<Int, ConcurrentSkipListSet<Int>>()

    // TODO: add FilesChanges[fileId] = Set(commit1, ...)
    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        val git = Git(repository)
        val reader = repository.newObjectReader()

        val userEmail = currCommit.authorIdent.emailAddress
        val userId = UserMapper.add(userEmail)
        val changedFiles = UtilGitMiner.getChangedFiles(currCommit, prevCommit, reader, git)

        for (fileId in changedFiles) {
            userFilesIds.computeIfAbsent(userId) { ConcurrentSkipListSet() }.add(fileId)
        }
    }


    override fun saveToJson(resourceDirectory: File) {
        val map = hashMapOf<Int, MutableSet<Int>>()
        for (entry in userFilesIds.entries) {
            map[entry.key] = entry.value
        }

        UtilFunctions.saveToJson(File(resourceDirectory, ProjectConfig.USER_FILES_IDS), map)
        Mapper.saveAll(resourceDirectory)
    }
}
