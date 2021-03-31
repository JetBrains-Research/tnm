package gitMiners

import kotlinx.serialization.builtins.serializer
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevCommit
import util.ProjectConfig
import util.UtilFunctions
import util.serialization.ConcurrentHashMapSerializer
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
    neededBranches: Set<String> = ProjectConfig.DEFAULT_NEEDED_BRANCHES,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) : GitMiner(repository, neededBranches, numThreads = numThreads) {


    private val assignmentMatrix: ConcurrentHashMap<Int, ConcurrentHashMap<Int, Int>> = ConcurrentHashMap()
    private val serializer = ConcurrentHashMapSerializer(
        Int.serializer(),
        ConcurrentHashMapSerializer(Int.serializer(), Int.serializer())
    )


    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        val git = threadLocalGit.get()
        val reader = threadLocalReader.get()

        reader.use {
            val changedFiles = UtilGitMiner.getChangedFiles(currCommit, prevCommit, it, git, userMapper, fileMapper)
            val userId = userMapper.add(currCommit.authorIdent.emailAddress)
            for (fileId in changedFiles) {
                assignmentMatrix
                    .computeIfAbsent(userId) { ConcurrentHashMap() }
                    .compute(fileId) { _, v -> if (v == null) 1 else v + 1 }
            }
        }

    }

    override fun saveToJson(resourceDirectory: File) {
        UtilFunctions.saveToJson(
            File(resourceDirectory, ProjectConfig.ASSIGNMENT_MATRIX),
            assignmentMatrix, serializer
        )
        saveMappers(resourceDirectory)
    }
}
