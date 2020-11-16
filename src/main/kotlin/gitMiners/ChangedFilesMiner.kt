package gitMiners

import com.google.gson.Gson
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.revwalk.RevCommit
import util.FileMapper
import util.ProjectConfig
import util.UserMapper
import java.io.File

class ChangedFilesMiner(override val repository: FileRepository) : GitMiner {
    override val git = Git(repository)
    override val reader: ObjectReader = repository.newObjectReader()
    override val gson: Gson = Gson()

    private val userFilesIds = hashMapOf<Int, MutableSet<Int>>()
    var lastProcessResult: List<Int> = emptyList()
        private set

    // TODO: add FilesChanges[fileId] = Set(commit1, ...)
    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        val result = mutableSetOf<Int>()

        val diffs = getDiffs(currCommit, prevCommit)

        val userEmail = currCommit.authorIdent.emailAddress
        val userId = UserMapper.add(userEmail)

        for (entry in diffs) {
            val fileId = FileMapper.add(entry.oldPath)
            userFilesIds.computeIfAbsent(userId) { mutableSetOf() }.add(fileId)
            result.add(fileId)
        }

        lastProcessResult = result.toList()
    }

    override fun saveToJson() {
        File("./resources/userFilesIds").writeText(gson.toJson(userFilesIds))
    }
}

fun main() {
    val parseChangedFiles = ChangedFilesMiner(ProjectConfig.repository)
    parseChangedFiles.run()
    FileMapper.saveToJson()
    UserMapper.saveToJson()
}