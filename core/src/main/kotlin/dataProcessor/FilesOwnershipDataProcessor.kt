package dataProcessor

import dataProcessor.initData.LatestCommitOwnedLines
import dataProcessor.inputData.FileLinesAddedByUser
import kotlinx.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class FilesOwnershipDataProcessor(private val decayFactor: Float = DEFAULT_DECAY_FACTOR) :
    DataProcessorMappedWithInit<LatestCommitOwnedLines, FileLinesAddedByUser>() {

    companion object {
        const val DEFAULT_DECAY_FACTOR = 0.01f
    }

    private val log: Logger = LoggerFactory.getLogger(FilesOwnershipDataProcessor::class.java)
    private lateinit var latestCommitDate: Date

    // [fileId][userId]
    private val _filesOwnership: ConcurrentHashMap<Int, ConcurrentHashMap<Int, UserData>> = ConcurrentHashMap()
    val filesOwnership: Map<Int, Map<Int, UserData>>
        get() = _filesOwnership

    // denoting the total potential authorship amount of all developers
    // [fileId]
    private val _potentialAuthorship = mutableMapOf<Int, Int>()
    val potentialAuthorship: Map<Int, Int>
        get() = _potentialAuthorship

    // denoting the total potential authorship amount of all developers
    // [userId][fileId]
    private val _developerKnowledge: HashMap<Int, HashMap<Int, Float>> = HashMap()
    val developerKnowledge: Map<Int, Map<Int, Float>>
        get() = _developerKnowledge

    // [fileId][line] = set(userId, ...)
    private val authorsForLine: ConcurrentHashMap<Int, ConcurrentHashMap<Int, ConcurrentSkipListSet<Int>>> =
        ConcurrentHashMap()

    @Serializable
    class UserData {
        private val ownedLines = mutableSetOf<Int>()
        var authorship: Float = 0f
            private set

        fun calculateAuthorship(lines: IntRange, decay: Float) {
            val newLines = lines - ownedLines
            if (newLines.isNotEmpty()) {
                authorship += newLines.size * decay
                ownedLines += newLines
            }
        }

    }

    override fun init(initData: LatestCommitOwnedLines) {
        latestCommitDate = initData.latestCommitDate

        for (entity in initData.linesOwnedByUser) {
            val fileId = fileMapper.add(entity.filePath)
            val userId = userMapper.add(entity.user)

            calculateAuthorshipForLines(entity.lineNumber..entity.lineNumber, fileId, userId, 1.0f)
            addAuthorsForLines(entity.lineNumber..entity.lineNumber, fileId, userId)
        }
    }

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

    private fun calculateAuthorshipForLines(lines: IntRange, fileId: Int, userId: Int, decay: Float) {
        _filesOwnership
            .computeIfAbsent(fileId) { ConcurrentHashMap() }
            .computeIfAbsent(userId) { UserData() }
            .calculateAuthorship(lines, decay)
    }

    private fun calculatePotentialAuthorship() {
        log.info("Start calculating potential authorship")
        for (fileEntry in authorsForLine) {
            val fileId = fileEntry.key
            var potentialAuthorshipForFile = 0
            for (lineEntry in fileEntry.value) {
                potentialAuthorshipForFile += lineEntry.value.size
            }
            _potentialAuthorship[fileId] = potentialAuthorshipForFile
        }
        log.info("End calculating potential authorship")
    }

    private fun calculateDeveloperKnowledge() {
        log.info("Start calculating developer knowledge")
        for (entryOwnership in _filesOwnership) {
            val fileId = entryOwnership.key
            for (entryUserData in entryOwnership.value) {
                val userId = entryUserData.key
                val userData = entryUserData.value
                _developerKnowledge
                    .computeIfAbsent(userId) { HashMap() }[fileId] =
                    userData.authorship / _potentialAuthorship[fileId]!!
            }
        }
        log.info("End calculating developer knowledge")
    }
}
