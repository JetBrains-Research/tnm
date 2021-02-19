package gitMiners

import kotlinx.serialization.builtins.serializer
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevCommit
import util.Mapper
import util.ProjectConfig
import util.UtilFunctions
import util.serialization.ConcurrentHashMapSerializer
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Class for mining  file dependency matrix
 * For example:
 * Change sets {A,B,C} and {A,B} the dependency matrix entries in D would be
 * D[A,B] = 2, D[A,C] = 1, and D[B,C] = 1
 *
 */
class FileDependencyMatrixMiner(
    repository: FileRepository,
    neededBranches: Set<String> = ProjectConfig.DEFAULT_NEEDED_BRANCHES,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) : GitMiner(repository, neededBranches, numThreads = numThreads) {

    private val fileDependencyMatrix: ConcurrentHashMap<Int, ConcurrentHashMap<Int, Int>> =
        ConcurrentHashMap()
    private val serializer = ConcurrentHashMapSerializer(
        Int.serializer(),
        ConcurrentHashMapSerializer(Int.serializer(), Int.serializer())
    )

    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        val git = Git(repository)
        val reader = repository.newObjectReader()

        val listOfChangedFiles = UtilGitMiner.getChangedFiles(currCommit, prevCommit, reader, git).toList()
        for ((index, currFile) in listOfChangedFiles.withIndex()) {
            for (otherFile in listOfChangedFiles.subList(index, listOfChangedFiles.lastIndex)) {
                if (currFile == otherFile)
                    continue
                increment(currFile, otherFile)
                increment(otherFile, currFile)
            }
        }
    }

    private fun increment(fileId1: Int, fileId2: Int) {
        fileDependencyMatrix
            .computeIfAbsent(fileId1) { ConcurrentHashMap() }
            .compute(fileId2) { _, v -> if (v == null) 1 else v + 1 }
    }

    override fun saveToJson(resourceDirectory: File) {
        UtilFunctions.saveToJson(
            File(resourceDirectory, ProjectConfig.FILE_DEPENDENCY),
            fileDependencyMatrix, serializer
        )
        Mapper.saveAll(resourceDirectory)
    }
}
