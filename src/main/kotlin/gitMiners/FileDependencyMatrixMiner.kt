package gitMiners

import com.google.gson.Gson
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.revwalk.RevCommit
import util.FileMapper
import java.io.File

/**
 * Class for parsing  file dependency matrix based on https://ieeexplore.ieee.org/abstract/document/5740929
 * For example:
 * Change sets {A,B,C} and {A,B} the dependency matrix entries in D would be
 * D[A,B] = 2, D[A,C] = 1, and D[B,C] = 1
 */
class FileDependencyMatrixMiner(override val repository: FileRepository) : GitMiner() {
    override val git = Git(repository)
    override val reader: ObjectReader = repository.newObjectReader()
    override val gson: Gson = Gson()

    private val changedFilesMiner = ChangedFilesMiner(repository)
    private val changedFiles = mutableListOf<List<Int>>()
    private lateinit var fileDependencyMatrix: Array<Array<Int>>

    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        changedFiles.add(getChangedFiles(currCommit, prevCommit, reader, git).toList())
    }

    override fun run() {
        super.run()

        val size = FileMapper.lastFileId
        fileDependencyMatrix = Array(size) { Array(size) { 0 } }
        for (change in changedFiles) {
            for ((index, currFile) in change.withIndex()) {
                for (otherFile in change.subList(index, change.lastIndex)) {
                    if (currFile == otherFile)
                        continue
                    fileDependencyMatrix[currFile][otherFile] += 1
                    fileDependencyMatrix[otherFile][currFile] += 1
                }
            }
        }
    }

    override fun saveToJson() {
        changedFilesMiner.saveToJson()
        File("./resources/fileDependencyMatrix").writeText(gson.toJson(fileDependencyMatrix))
    }
}