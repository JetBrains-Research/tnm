package dataProcessor

import dataProcessor.ComplexityCodeChangesDataProcessor.FileModification
import kotlinx.serialization.Serializable
import util.FileMapper
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.log2

class ComplexityCodeChangesDataProcessor(
    val periodType: PeriodType = DEFAULT_PERIOD_TYPE,
    val changeType: ChangeType = DEFAULT_CHANGE_TYPE,
    val numOfCommitsInPeriod: Int = DEFAULT_NUM_COMMITS,
    val numOfMonthInPeriod: Int = DEFAULT_NUM_MONTH
) : DataProcessor<FileModification> {

    companion object {
        const val DEFAULT_NUM_MONTH = 1
        const val DEFAULT_NUM_COMMITS = 500
        val DEFAULT_PERIOD_TYPE = PeriodType.MODIFICATION_LIMIT
        val DEFAULT_CHANGE_TYPE = ChangeType.LINES
    }

    enum class PeriodType { TIME_BASED, MODIFICATION_LIMIT }
    enum class ChangeType { FILE, LINES }

    val fileMapper = FileMapper()

    // HCPF1 is equal to periodEntropy
    @Serializable
    data class FileStats(
        val entropy: Double,
        val HCPF2: Double,
        val HCPF3: Double
    )

    @Serializable
    data class PeriodStats(val periodEntropy: Double, val filesStats: HashMap<Int, FileStats>)

    // Counter of changed files of period
    // [period][fileId] = num of changes
    private val periodToFileChanges = ConcurrentHashMap<Int, ConcurrentHashMap<Int, Int>>()
    val periodsToStats = HashMap<Int, PeriodStats>()

    data class FileModification(val periodId: Int, val filePath: String, val modifications: Int)

    override fun processData(data: FileModification) {
        val fileId = fileMapper.add(data.filePath)
        periodToFileChanges
            .computeIfAbsent(data.periodId) { ConcurrentHashMap() }
            .compute(fileId) { _, v -> if (v == null) data.modifications else v + data.modifications }
    }

    override fun calculate() {
        for ((periodId, changes) in periodToFileChanges) {
            var numOfAllChanges = 0
            for (numOfChanges in changes.values) {
                numOfAllChanges += numOfChanges
            }

            // Calculate entropy for each file and overall
            val filesEntropy = mutableListOf<Triple<Int, Double, Double>>()
            var periodEntropy = 0.0
            for ((fileId, numOfChanges) in changes) {
                val p = numOfChanges.toDouble() / numOfAllChanges
                val entropy = -p * log2(p)
                periodEntropy += entropy
                filesEntropy.add(Triple(fileId, entropy, p))
            }

            // Calculate factors
            val numOfFilesInPeriod = changes.size
            val filesStats = HashMap<Int, FileStats>()
            for ((fileId, entropy, p) in filesEntropy) {
                filesStats[fileId] =
                    FileStats(
                        entropy,
                        p * periodEntropy,
                        (1.0 / numOfFilesInPeriod) * periodEntropy
                    )

            }

            periodsToStats[periodId] = PeriodStats(periodEntropy, filesStats)
        }
    }


}