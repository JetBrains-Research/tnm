package gitMiners

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.revwalk.RevCommit
import util.ProjectConfig
import util.UserMapper
import util.UtilFunctions

/**
 * Assignment matrix miner
 *
 * @property repository
 * @constructor Create empty Assignment matrix miner for [repository] and store the results
 */
class AssignmentMatrixMiner(override val repository: FileRepository) : GitMiner() {
    override val git = Git(repository)
    override val reader: ObjectReader = repository.newObjectReader()

    private val assignmentMatrix: HashMap<Int, HashMap<Int, Int>> = HashMap()

    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        val changedFiles = getChangedFiles(currCommit, prevCommit, reader, git)
        val userId = UserMapper.add(currCommit.authorIdent.emailAddress)
        for (fileId in changedFiles) {
            val newValue = assignmentMatrix
                .computeIfAbsent(userId) { HashMap() }
                .computeIfAbsent(fileId) { 0 }
                .inc()

            assignmentMatrix
                .computeIfAbsent(userId) { HashMap() }[fileId] = newValue
        }

    }

    override fun saveToJson() {
        UtilFunctions.saveToJson(ProjectConfig.ASSIGNMENT_MATRIX_PATH, assignmentMatrix)
    }
}