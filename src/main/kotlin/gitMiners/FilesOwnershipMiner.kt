package gitMiners

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.util.io.DisabledOutputStream
import util.FileMapper
import util.ProjectConfig
import util.UserMapper
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.pow


// Class based on paper:
// "Engaging developers in open source software projects: harnessing social and technical data
// mining to improve software development"
// https://lib.dr.iastate.edu/cgi/viewcontent.cgi?article=5670&context=etd
// TODO: Double, Float?
class FilesOwnershipMiner(override val repository: FileRepository) : GitMiner() {
    override val git = Git(repository)
    override val reader: ObjectReader = repository.newObjectReader()

    private val diffFormatter = DiffFormatter(DisabledOutputStream.INSTANCE)
    // [fileId][userId]
    private val filesOwnership: HashMap<Int, HashMap<Int, UserData>> = HashMap()
    // [fileId][line] = set(userId, ...)
    private val authorsForLine: HashMap<Int, HashMap<Int, HashSet<Int>>> = HashMap()
    // denoting the total potential authorship amount of all developers
    // [fileId]
    private val potentialAuthorship = mutableMapOf<Int, Int>()
    // denoting the total potential authorship amount of all developers
    // [userId][fileId]
    private val developerKnowledge: HashMap<Int, HashMap<Int, Double>> = HashMap()


    val decayFactor = 0.01
    // TODO: think about branches
    private val latestCommit = git.log().setMaxCount(1).call().iterator().next()
    private val latestCommitDate = latestCommit.authorIdent.getWhen()

    init {
        diffFormatter.setRepository(repository)
        diffFormatter.setDiffComparator(RawTextComparator.DEFAULT)
        diffFormatter.isDetectRenames = true
    }

    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        val diffs = getDiffs(currCommit, prevCommit, reader, git)
        val email = currCommit.authorIdent.emailAddress

        val userId = UserMapper.add(email)

        for (diff in diffs) {
            val editList = diffFormatter.toFileHeader(diff).toEditList()

            val fileId = FileMapper.add(diff.oldPath)

            val date = currCommit.authorIdent.getWhen()

            // Long -> Int
            val diffDays: Int = TimeUnit.DAYS.convert(latestCommitDate.time - date.time, TimeUnit.MILLISECONDS).toInt()
            for (edit in editList) {
                // TODO: what about deleted lines?
                // TODO: Wrong calculation?? doesn't saves the result?
                filesOwnership
                        .computeIfAbsent(fileId) { HashMap() }
                        .computeIfAbsent(userId) { UserData() }
                        .calculateAuthorship(edit.beginB..edit.endB, diffDays)
                addAuthorsForLines(edit.beginB..edit.endB, fileId, userId)
                //                linesDeleted += edit.endA - edit.beginA
//                linesAdded += edit.endB - edit.beginB

            }
        }
    }

    override fun run() {
        processHead()
        super.run()
        calculatePotentialAuthorship()
        calculateDeveloperKnowledge()
    }

    private fun processHead() {
        val treeWalk = TreeWalk(repository)
        treeWalk.addTree(latestCommit.tree)
        treeWalk.isRecursive = false
        while (treeWalk.next()) {
            if (treeWalk.isSubtree) {
                treeWalk.enterSubtree()
                continue
            }

            val filePath = treeWalk.pathString
            val fileId = FileMapper.add(filePath)

            val result = git
                    .blame()
                    .setFilePath(filePath)
                    .setTextComparator(RawTextComparator.WS_IGNORE_ALL).call()

            val rawText = result.resultContents

            for (i in 0 until rawText.size()) {
                val sourceAuthor = result.getSourceAuthor(i)
//                val sourceCommit = result.getSourceCommit(i)

                val userId = UserMapper.add(sourceAuthor.emailAddress)

                // TODO: Wrong calculation?? doesn't saves the result?
                filesOwnership
                        .computeIfAbsent(fileId) { HashMap() }
                        .computeIfAbsent(userId) { UserData() }
                        .calculateAuthorship(i..i, 0)
                addAuthorsForLines(i..i, fileId, userId)
            }
        }
    }

    override fun saveToJson() {
        File("./resources/filesOwnership").writeText(Json.encodeToString(filesOwnership))
        File("./resources/potentialAuthorship").writeText(Json.encodeToString(potentialAuthorship))
        File("./resources/developerKnowledge").writeText(Json.encodeToString(developerKnowledge))
    }

    private fun addAuthorsForLines(lines: IntRange, fileId:Int, userId: Int) {
        for (line in lines) {
            authorsForLine
                    .computeIfAbsent(fileId) { HashMap() }
                    .computeIfAbsent(userId) { HashSet() }
                    .add(line)
        }
    }

    private fun calculatePotentialAuthorship() {
        for (fileEntry in authorsForLine) {
            val fileId = fileEntry.key
            var potentialAuthorshipForFile = 0
            for (lineEntry in fileEntry.value) {
                potentialAuthorshipForFile += lineEntry.value.size
            }
            potentialAuthorship[fileId] = potentialAuthorshipForFile
        }
    }

    private fun calculateDeveloperKnowledge() {
        for (entryOwnership in filesOwnership) {
            val fileId = entryOwnership.key
            for (entryUserData in entryOwnership.value) {
                val userId = entryUserData.key
                val userData = entryUserData.value
                developerKnowledge
                        .computeIfAbsent(userId) { HashMap() }[fileId] = userData.authorship / potentialAuthorship[fileId]!!
            }
        }
    }

    inner class UserData {
        private val ownedLines = mutableSetOf<Int>()
        var authorship: Double = 0.0

        fun calculateAuthorship(lines: IntRange, days: Int) {
            val newLines = lines - ownedLines
            if (newLines.isNotEmpty()) {
                authorship += newLines.size * (1 - decayFactor).pow(days)
                ownedLines += newLines
            }
        }

    }
}

fun main() {
    val parse = FilesOwnershipMiner(ProjectConfig.repository)
    parse.run()
    parse.saveToJson()
    FileMapper.saveToJson()
    UserMapper.saveToJson()
}