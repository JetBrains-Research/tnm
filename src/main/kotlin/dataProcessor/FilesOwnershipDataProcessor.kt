package dataProcessor

import dataProcessor.FilesOwnershipDataProcessor.FileLinesAddedByUser
import dataProcessor.FilesOwnershipDataProcessor.InitData
import kotlinx.serialization.Serializable
import util.CommitMapper
import util.FileMapper
import util.UserMapper
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap
import kotlin.math.pow

class FilesOwnershipDataProcessor : DataProcessorWithInit<InitData, FileLinesAddedByUser> {
    override val userMapper = UserMapper()
    override val fileMapper = FileMapper()
    override val commitMapper = CommitMapper()

    private lateinit var latestCommitDate: Date

    // [fileId][userId]
    val filesOwnership: ConcurrentHashMap<Int, ConcurrentHashMap<Int, UserData>> = ConcurrentHashMap()

    // denoting the total potential authorship amount of all developers
    // [fileId]
    val potentialAuthorship = mutableMapOf<Int, Int>()

    // denoting the total potential authorship amount of all developers
    // [userId][fileId]
    val developerKnowledge: HashMap<Int, HashMap<Int, Double>> = HashMap()

    // [fileId][line] = set(userId, ...)
    private val authorsForLine: ConcurrentHashMap<Int, ConcurrentHashMap<Int, ConcurrentSkipListSet<Int>>> =
        ConcurrentHashMap()

    private val decayFactor = 0.01

    @Serializable
    class UserData {
        private val ownedLines = mutableSetOf<Int>()
        var authorship: Double = 0.0
            private set

        fun calculateAuthorship(lines: IntRange, decay: Double) {
            val newLines = lines - ownedLines
            if (newLines.isNotEmpty()) {
                authorship += newLines.size * decay
                ownedLines += newLines
            }
        }

    }

    data class FileLineOwnedByUser(val lineNumber: Int, val filePath: String, val user: String)
    data class InitData(val latestCommitDate: Date, val linesOwnedByUser: List<FileLineOwnedByUser>)

    override fun init(initData: InitData) {
        latestCommitDate = initData.latestCommitDate

        for (entity in initData.linesOwnedByUser) {
            val fileId = fileMapper.add(entity.filePath)
            val userId = userMapper.add(entity.user)

            calculateAuthorshipForLines(entity.lineNumber..entity.lineNumber, fileId, userId, 1.0)
            addAuthorsForLines(entity.lineNumber..entity.lineNumber, fileId, userId)
        }
    }

    data class FileLinesAddedByUser(val addedLines: IntRange, val filePath: String, val user: String, val date: Date)

    override fun processData(data: FileLinesAddedByUser) {
        val diffDays: Int = TimeUnit.DAYS.convert(
            latestCommitDate.time - data.date.time,
            TimeUnit.MILLISECONDS
        ).toInt()

        val decay = (1 - decayFactor).pow(diffDays)

        val fileId = fileMapper.add(data.filePath)
        val userId = userMapper.add(data.user)

        // TODO: what about deleted lines?
        calculateAuthorshipForLines(data.addedLines, fileId, userId, decay)
        addAuthorsForLines(data.addedLines, fileId, userId)
    }

    override fun calculate() {
        calculatePotentialAuthorship()
        calculateDeveloperKnowledge()
    }

    private fun addAuthorsForLines(lines: IntRange, fileId: Int, userId: Int) {
        authorsForLine
            .computeIfAbsent(fileId) { ConcurrentHashMap() }
            .computeIfAbsent(userId) { ConcurrentSkipListSet() }
            .addAll(lines)
    }

    private fun calculateAuthorshipForLines(lines: IntRange, fileId: Int, userId: Int, decay: Double) {
        filesOwnership
            .computeIfAbsent(fileId) { ConcurrentHashMap() }
            .computeIfAbsent(userId) { UserData() }
            .calculateAuthorship(lines, decay)
    }

    private fun calculatePotentialAuthorship() {
        println("Start calculating potential authorship")
        for (fileEntry in authorsForLine) {
            val fileId = fileEntry.key
            var potentialAuthorshipForFile = 0
            for (lineEntry in fileEntry.value) {
                potentialAuthorshipForFile += lineEntry.value.size
            }
            potentialAuthorship[fileId] = potentialAuthorshipForFile
        }
        println("End calculating potential authorship")
    }

    private fun calculateDeveloperKnowledge() {
        println("Start calculating developer knowledge")
        for (entryOwnership in filesOwnership) {
            val fileId = entryOwnership.key
            for (entryUserData in entryOwnership.value) {
                val userId = entryUserData.key
                val userData = entryUserData.value
                developerKnowledge
                    .computeIfAbsent(userId) { HashMap() }[fileId] =
                    userData.authorship / potentialAuthorship[fileId]!!
            }
        }
        println("End calculating developer knowledge")
    }
}
