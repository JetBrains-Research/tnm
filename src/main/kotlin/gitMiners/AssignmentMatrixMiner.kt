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


/**
 * Assignment matrix miner
 *
 * @property repository
 * @constructor Create empty Assignment matrix miner for [repository] and store the results
 */
class AssignmentMatrixMiner(
    repository: FileRepository,
    neededBranches: Set<String> = ProjectConfig.neededBranches,
    numThreads: Int = ProjectConfig.numThreads
) : GitMiner(repository, neededBranches, numThreads = numThreads) {

    private val assignmentMatrix: ConcurrentHashMap<Int, ConcurrentHashMap<Int, Int>> = ConcurrentHashMap()

    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        val git = Git(repository)
        val reader = repository.newObjectReader()

        val changedFiles = UtilGitMiner.getChangedFiles(currCommit, prevCommit, reader, git)
        val userId = UserMapper.add(currCommit.authorIdent.emailAddress)
        for (fileId in changedFiles) {
            assignmentMatrix
                .computeIfAbsent(userId) { ConcurrentHashMap() }
                .compute(fileId) { _, v -> if (v == null) 1 else v + 1 }
        }
    }

    override fun saveToJson(resourceDirectory: File) {
        UtilFunctions.saveToJson(
            File(resourceDirectory, ProjectConfig.ASSIGNMENT_MATRIX),
            UtilFunctions.convertConcurrentMapOfConcurrentMapsInt(assignmentMatrix)
        )
        Mapper.saveAll(resourceDirectory)
    }
}
