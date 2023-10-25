package dataProcessor

import dataProcessor.inputData.CommitFilesModifications
import kotlinx.serialization.Serializable
import util.TrimmedDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.log2

class ComplexityCodeChangesDataProcessor(
    val periodType: PeriodType = DEFAULT_PERIOD_TYPE,
    val changeType: ChangeType = DEFAULT_CHANGE_TYPE,
) : DataProcessorMappedWithInit<TrimmedDate, CommitFilesModifications>() {

    companion object {
        val DEFAULT_PERIOD_TYPE = PeriodType.MONTH
        val DEFAULT_CHANGE_TYPE = ChangeType.LINES
    }

    enum class PeriodType { MONTH, WEEK}
    enum class ChangeType { FILE, LINES }

    // HCPF1 is equal to periodEntropy
    @Serializable
    data class FileStats(
        val entropy: Double,
        val HCPF2: Double,
        val HCPF3: Double
    )

    @Serializable
    data class PeriodStats(val periodEntropy: Double, val filesStats: Map<Int, FileStats>)

    // Counter of changed files of period
    // [period][fileId] = num of changes
    private val periodToFileChanges = ConcurrentHashMap<Int, ConcurrentHashMap<Int, Int>>()

    private val _periodsToStats = HashMap<Int, PeriodStats>()
    val periodsToStats: Map<Int, PeriodStats>
        get() = _periodsToStats

    private lateinit var latestCommitDate: TrimmedDate

    override fun processData(data: CommitFilesModifications) {
        val periodId = getPeriod(data.trimmedDate)
        for (fileModification in data.filesModifications) {
            val fileId = fileMapper.add(fileModification.filePath)
            periodToFileChanges
                .computeIfAbsent(periodId) { ConcurrentHashMap() }
                .compute(fileId) { _, v -> if (v == null) fileModification.modifications else v + fileModification.modifications }
        }
    }

    override fun init(initData: TrimmedDate) {
        latestCommitDate = initData
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
            val maxEntropy = log2(numOfAllChanges.toDouble())
            for ((fileId, numOfChanges) in changes) {
                val p = numOfChanges.toDouble() / numOfAllChanges
                val entropy = if (maxEntropy == 0.0) 0.0 else -p * log2(p) / maxEntropy
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

            _periodsToStats[periodId] = PeriodStats(periodEntropy, filesStats)
        }
    }

    private fun getPeriod(commitTrimmedDate: TrimmedDate): Int {
        return when (periodType) {
            PeriodType.MONTH -> {
                val months = commitTrimmedDate.diffInMonthIgnoreDays(latestCommitDate)
                months
            }

            PeriodType.WEEK -> {
                val weeks = commitTrimmedDate.diffInWeeks(latestCommitDate)
                weeks
            }
        }
    }

    fun getTimePeriodOfPeriodId(periodId: Int): Pair<TrimmedDate, TrimmedDate> {
        val latestLocalDate = latestCommitDate.toLocalDate()
        return when (periodType) {
            PeriodType.MONTH -> {
                val newDate = latestLocalDate.minusMonths(periodId.toLong())
                val yearMonth = YearMonth.of(newDate.year, newDate.month)
                val startMonth = TrimmedDate.fromLocalDate(yearMonth.atDay(1))
                val endMonth = TrimmedDate.fromLocalDate(yearMonth.atEndOfMonth())
                (startMonth to endMonth)
            }

            PeriodType.WEEK -> {
                val isoField = WeekFields.of(Locale.GERMANY).dayOfWeek()
                val newDate = latestLocalDate.minusWeeks(periodId.toLong())
                val startWeek = TrimmedDate.fromLocalDate(newDate.with(isoField, 1))
                val endWeek = TrimmedDate.fromLocalDate(newDate.with(isoField, 7))
                (startWeek to endWeek)
            }
        }
    }

}