package gitMiners

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevCommit
import util.Mapper
import util.ProjectConfig
import util.UtilFunctions
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Class for mining  file dependency matrix
 * based on https://ieeexplore.ieee.org/abstract/document/5740929
 * For example:
 * Change sets {A,B,C} and {A,B} the dependency matrix entries in D would be
 * D[A,B] = 2, D[A,C] = 1, and D[B,C] = 1
 *
 */
class FileDependencyMatrixMiner(
    repository: FileRepository,
    neededBranches: Set<String> = ProjectConfig.neededBranches,
    numThreads: Int = ProjectConfig.numThreads
) : GitMiner(repository, neededBranches, numThreads = numThreads) {

    private val fileDependencyMatrix: ConcurrentHashMap<Int, ConcurrentHashMap<Int, AtomicInteger>> =
        ConcurrentHashMap()

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

    override fun run() {
        multithreadingRun()
    }

    private fun increment(fileId1: Int, fileId2: Int) {
        fileDependencyMatrix
            .computeIfAbsent(fileId1) { ConcurrentHashMap() }
            .computeIfAbsent(fileId2) { AtomicInteger(0) }
            .incrementAndGet()

    }

    override fun saveToJson(resourceDirectory: File) {
        UtilFunctions.saveToJson(
            File(resourceDirectory, ProjectConfig.FILE_DEPENDENCY),
            UtilFunctions.convertConcurrentMapOfConcurrentMaps(fileDependencyMatrix)
        )
        Mapper.saveAll(resourceDirectory)
    }
}
