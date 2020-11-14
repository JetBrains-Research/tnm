package gitParsers

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
class FileDependencyMatrixMiner(override val repository: FileRepository) : GitMiner {
    override val git = Git(repository)
    override val reader: ObjectReader = repository.newObjectReader()
    override val gson: Gson = Gson()

    private val changedFilesParser = ChangedFilesMiner(repository)
    private val cache = mutableListOf<List<Int>>()
    private lateinit var matrix: Array<Array<Int>>

    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        changedFilesParser.process(currCommit, prevCommit)
        val changedFiles = changedFilesParser.lastProcessResult
        cache.add(changedFiles)
    }

    override fun run() {
        super.run()

        val size = FileMapper.lastFileId
        matrix = Array(size) { Array(size) { 0 } }
        for (change in cache) {
            for ((index, currFile) in change.withIndex()) {
                for (otherFile in change.subList(index, change.lastIndex)) {
                    if (currFile == otherFile)
                        continue
                    matrix[currFile][otherFile] += 1
                    matrix[otherFile][currFile] += 1
                }
            }
        }
    }

    override fun saveToJson() {
        changedFilesParser.saveToJson()
        File("./resources/fileDependencyMatrix").writeText(gson.toJson(matrix))
    }
}