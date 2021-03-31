package gitMiners

import gitMiners.UtilGitMiner.isBugFixCommit
import kotlinx.serialization.Serializable
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.util.io.DisabledOutputStream
import util.ProjectConfig
import util.UtilFunctions
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap
import kotlin.math.log2


class ComplexityCodeChangesMiner(
    repository: FileRepository,
    private val neededBranch: String = ProjectConfig.DEFAULT_BRANCH,
    private val periodType: PeriodType = DEFAULT_PERIOD_TYPE,
    private val changeType: ChangeType = DEFAULT_CHANGE_TYPE,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS,
    private val numOfCommitsInPeriod: Int = DEFAULT_NUM_COMMITS,
    private val numOfMonthInPeriod: Int = DEFAULT_NUM_MONTH
) : GitMiner(repository, setOf(neededBranch), numThreads = numThreads) {

    companion object {
        const val DEFAULT_NUM_MONTH = 1
        const val DEFAULT_NUM_COMMITS = 500
        val DEFAULT_PERIOD_TYPE = PeriodType.MODIFICATION_LIMIT
        val DEFAULT_CHANGE_TYPE = ChangeType.LINES
    }

    enum class PeriodType { TIME_BASED, MODIFICATION_LIMIT }
    enum class ChangeType { FILE, LINES }

    // Mark each commit for period
    // [commitId][periodId]
    private val markedCommits = ConcurrentHashMap<Int, Int>()

    // Counter of changed files of period
    // [period][fileId] = num of changes
    private val periodToFileChanges = ConcurrentHashMap<Int, ConcurrentHashMap<Int, Int>>()

    private val periodsToStats = HashMap<Int, PeriodStats>()

    // HCPF1 is equal to periodEntropy
    @Serializable
    data class FileStats(
        val entropy: Double,
        val HCPF2: Double,
        val HCPF3: Double
    )

    @Serializable
    data class PeriodStats(val periodEntropy: Double, val filesStats: HashMap<Int, FileStats>)

    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        if (!isFeatureIntroductionCommit(currCommit)) return

        val git = threadLocalGit.get()
        val reader = threadLocalReader.get()

        val currCommitId = commitMapper.add(currCommit.name)
        val periodId = markedCommits[currCommitId]!!

        when (changeType) {
            ChangeType.LINES -> {
                val diffs = reader.use { UtilGitMiner.getDiffsWithoutText(currCommit, prevCommit, it, git) }
                val diffFormatter = DiffFormatter(DisabledOutputStream.INSTANCE)
                diffFormatter.setRepository(repository)
                diffFormatter.setDiffComparator(RawTextComparator.DEFAULT)
                diffFormatter.isDetectRenames = true

                for (diff in diffs) {
                    val fileId = fileMapper.add(diff.oldPath)

                    val editList = diffFormatter.toFileHeader(diff).toEditList()
                    for (edit in editList) {
                        val numOfAddedLines = edit.endB - edit.beginB
                        val numOfDeletedLines = edit.endA - edit.beginA

                        val modifiedLines = numOfAddedLines + numOfDeletedLines

                        periodToFileChanges
                            .computeIfAbsent(periodId) { ConcurrentHashMap() }
                            .compute(fileId) { _, v -> if (v == null) modifiedLines else v + modifiedLines }
                    }
                }
            }

            ChangeType.FILE -> {
                val changedFiles = reader.use {
                    UtilGitMiner.getChangedFiles(
                        currCommit, prevCommit, it, git, userMapper,
                        fileMapper
                    )
                }
                for (fileId in changedFiles) {
                    periodToFileChanges
                        .computeIfAbsent(periodId) { ConcurrentHashMap() }
                        .compute(fileId) { _, v -> if (v == null) 1 else v + 1 }
                }
            }
        }

    }

    override fun run() {
        UtilGitMiner.findNeededBranchesOrNull(threadLocalGit.get(), neededBranches) ?: return
        markCommits()
        super.run()
        calculateFactors()
    }

    override fun saveToJson(resourceDirectory: File) {
        UtilFunctions.saveToJson(
            File(resourceDirectory, ProjectConfig.COMPLEXITY_CODE),
            periodsToStats
        )
        saveMappers(resourceDirectory)
    }

    private fun isFeatureIntroductionCommit(commit: RevCommit): Boolean {
        return !isBugFixCommit(commit)
    }

    private fun splitInPeriods(): List<List<RevCommit>> {
        val git = threadLocalGit.get()
        val commitsInBranch = UtilGitMiner.getCommits(git, repository, neededBranch, reversed)
        if (commitsInBranch.isEmpty()) return listOf()

        return when (periodType) {
            PeriodType.TIME_BASED -> {
                val firstDate = getTrimDate(commitsInBranch.first())
                val periods = mutableListOf<List<RevCommit>>()

                var upperThreshold = addMonths(firstDate, -numOfMonthInPeriod)
                var period = mutableListOf<RevCommit>()
                for (commit in commitsInBranch) {
                    val commitDate = Date(commit.commitTime * 1000L)
                    if (commitDate < upperThreshold) {
                        periods.add(period)
                        period = mutableListOf()
                        upperThreshold = addMonths(upperThreshold, -numOfMonthInPeriod)
                    }
                    period.add(commit)
                }

                if (period.isNotEmpty()) {
                    periods.add(period)
                }

                periods
            }
            PeriodType.MODIFICATION_LIMIT -> commitsInBranch.chunked(numOfCommitsInPeriod)
        }
    }

    private fun getTrimDate(commit: RevCommit): Date {
        val date = Date(commit.commitTime * 1000L)

        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar[Calendar.MILLISECOND] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.HOUR_OF_DAY] = 0
        return calendar.time
    }

    private fun addMonths(date: Date, numOfMonth: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.MONTH, numOfMonth)
        return calendar.time
    }

    private fun markCommits() {
        val periods = splitInPeriods()
        for ((i, period) in periods.withIndex()) {
            for (commit in period) {
                val commitId = commitMapper.add(commit.name)
                markedCommits[commitId] = i
            }
        }
    }

    private fun calculateFactors() {
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
