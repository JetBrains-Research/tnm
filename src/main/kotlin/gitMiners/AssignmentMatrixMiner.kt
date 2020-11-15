package gitMiners

import com.google.gson.Gson
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.revwalk.RevCommit
import util.FileMapper
import util.UserMapper
import java.io.File

class AssignmentMatrixMiner(override val repository: FileRepository) : GitMiner {
    override val git = Git(repository)
    override val reader: ObjectReader = repository.newObjectReader()
    override val gson: Gson = Gson()

    private val changedFilesParser = ChangedFilesMiner(repository)
    private val cache = mutableListOf<Pair<String, List<Int>>>()
    private lateinit var matrix: Array<Array<Int>>

    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        changedFilesParser.process(currCommit, prevCommit)
        val changedFiles = changedFilesParser.lastProcessResult
        val userEmail = currCommit.authorIdent.emailAddress
        cache.add(userEmail to changedFiles)
    }

    override fun run() {
        super.run()

        val numOfFiles = FileMapper.lastFileId
        val numOfUsers = UserMapper.lastUserId
        matrix = Array(numOfUsers) { Array(numOfFiles) { 0 } }
        for (change in cache) {
            val userId = UserMapper.userToId[change.first]
            for (fileId in change.second) {
                userId?.let { matrix[userId][fileId] += 1 }
            }
        }
    }

    override fun saveToJson() {
        changedFilesParser.saveToJson()
        File("./resources/assignmentMatrix").writeText(gson.toJson(matrix))
    }
}