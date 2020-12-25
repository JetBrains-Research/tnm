package gitMiners

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.revwalk.RevCommit
import util.ProjectConfig
import util.UtilFunctions
import java.io.File

/**
 * Class for mining  file dependency matrix
 * based on https://ieeexplore.ieee.org/abstract/document/5740929
 * For example:
 * Change sets {A,B,C} and {A,B} the dependency matrix entries in D would be
 * D[A,B] = 2, D[A,C] = 1, and D[B,C] = 1
 *
 */
class FileDependencyMatrixMiner(override val repository: FileRepository) : GitMiner() {
    override val git = Git(repository)
    override val reader: ObjectReader = repository.newObjectReader()

    private val fileDependencyMatrix: HashMap<Int, HashMap<Int, Int>> = HashMap()

    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        val listOfChangedFiles = getChangedFiles(currCommit, prevCommit, reader, git).toList()
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
        val newValue = fileDependencyMatrix
            .computeIfAbsent(fileId1) { HashMap() }
            .computeIfAbsent(fileId2) { 0 }
            .inc()

        fileDependencyMatrix
            .computeIfAbsent(fileId1) { HashMap() }[fileId2] = newValue
    }

    override fun saveToJson(resourceDirectory: File) {
        UtilFunctions.saveToJson(File(resourceDirectory, ProjectConfig.FILE_DEPENDENCY), fileDependencyMatrix)
    }
}