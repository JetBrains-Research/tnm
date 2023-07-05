package miners.gitMiners

import dataProcessor.ComplexityCodeChangesDataProcessor
import dataProcessor.ComplexityCodeChangesDataProcessor.ChangeType
import dataProcessor.ComplexityCodeChangesDataProcessor.PeriodType
import dataProcessor.inputData.FileModification
import miners.gitMiners.GitMinerUtil.isBugFixCommit
import miners.gitMiners.GitMinerUtil.isNotNeededFilePath
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.util.io.DisabledOutputStream
import util.ProjectConfig
import util.TrimmedDate
import java.io.File
import java.util.concurrent.ConcurrentHashMap


class ComplexityCodeChangesMiner(
    repositoryFile: File,
    private val neededBranch: String,
    val filesToProceed: Set<String>? = null,
    numOfCommits: Int? = null,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) : GitMiner<ComplexityCodeChangesDataProcessor>(repositoryFile, setOf(neededBranch), numOfCommits, numThreads) {
    // Mark each commit for period
    // [commitId][periodId]
    private val markedCommits = ConcurrentHashMap<String, Int>()

    private val _periodToDate = mutableMapOf<Int, TrimmedDate>()
    val periodToDate: Map<Int, TrimmedDate>
        get() = _periodToDate

    override fun process(
        dataProcessor: ComplexityCodeChangesDataProcessor,
        commit: RevCommit
    ) {
        if (!isFeatureIntroductionCommit(commit)) return

        val reader = threadLocalReader.get()
        val periodId = markedCommits[commit.name]!!
        var containsNeededFiles = false

        when (dataProcessor.changeType) {
            ChangeType.LINES -> {
                val diffs = reader.use { GitMinerUtil.getDiffsWithoutText(commit, it, repository) }

                val diffFormatter = DiffFormatter(DisabledOutputStream.INSTANCE)
                diffFormatter.setRepository(repository)
                diffFormatter.setDiffComparator(RawTextComparator.DEFAULT)
                diffFormatter.isDetectRenames = true

                for (diff in diffs) {
                    val filePath = GitMinerUtil.getFilePath(diff)
                    if (isNotNeededFilePath(filePath, filesToProceed)) continue
                    containsNeededFiles = true

                    val editList = diffFormatter.toFileHeader(diff).toEditList()
                    for (edit in editList) {
                        val numOfAddedLines = edit.endB - edit.beginB
                        val numOfDeletedLines = edit.endA - edit.beginA

                        val modifiedLines = numOfAddedLines + numOfDeletedLines

                        val data = FileModification(periodId, filePath, modifiedLines)
                        dataProcessor.processData(data)
                    }
                }
            }

            ChangeType.FILE -> {
                val changedFiles = reader.use {
                    GitMinerUtil.getChangedFiles(
                        commit, it, repository
                    )
                }

                for (filePath in changedFiles) {
                    if (isNotNeededFilePath(filePath, filesToProceed)) continue
                    containsNeededFiles = true

                    val data = FileModification(periodId, filePath, 1)
                    dataProcessor.processData(data)
                }
            }
        }

        if (containsNeededFiles) {
            dataProcessor.incNumOfCommits(periodId)
        }

    }

    override fun run(dataProcessor: ComplexityCodeChangesDataProcessor) {
        GitMinerUtil.findNeededBranches(threadLocalGit.get(), neededBranches)
        markCommits(dataProcessor)

        super.run(dataProcessor)
        dataProcessor.calculate()
    }

    private fun isFeatureIntroductionCommit(commit: RevCommit): Boolean {
        return !isBugFixCommit(commit)
    }

    private fun splitInPeriods(dataProcessor: ComplexityCodeChangesDataProcessor): List<List<RevCommit>> {
        val git = threadLocalGit.get()
        val commitsInBranch = GitMinerUtil.getCommits(git, repository, neededBranch)
        if (commitsInBranch.isEmpty()) return listOf()

        return when (dataProcessor.periodType) {
            PeriodType.TIME_BASED -> {
                val periods = mutableListOf<List<RevCommit>>()
                var period = mutableListOf<RevCommit>()
                val sortedByDayCommits = commitsInBranch.sortedBy { TrimmedDate.getTrimDate(it) }
                for ((commit1, commit2) in sortedByDayCommits.windowed(2)) {
                    period.add(commit1)
                    if (!isSameTimePeriod(commit1, commit2, dataProcessor.numOfMonthInPeriod)) {
                        periods.add(period)
                        period = mutableListOf()
                    }
                }

                val lastCommit = sortedByDayCommits.last()
                if (isSameTimePeriod(period.last(), lastCommit, dataProcessor.numOfMonthInPeriod)) {
                    period.add(lastCommit)
                } else {
                    periods.add(period)
                    period = mutableListOf(lastCommit)
                }
                periods.add(period)
                periods
            }
            PeriodType.MODIFICATION_LIMIT -> commitsInBranch.chunked(dataProcessor.numOfCommitsInPeriod)
        }
    }

    private fun isSameTimePeriod(commit1: RevCommit, commit2: RevCommit, monthThreshold: Int): Boolean {
        val trimDate1 = TrimmedDate.getTrimDate(commit1)
        val trimDate2 = TrimmedDate.getTrimDate(commit2)
        return trimDate1.diffInMonth(trimDate2) < monthThreshold
    }

    private fun markCommits(dataProcessor: ComplexityCodeChangesDataProcessor) {
        val periods = splitInPeriods(dataProcessor)
        for ((i, period) in periods.withIndex()) {
            val month = TrimmedDate.getTrimDate(period.first())
            _periodToDate[i] = month
            for (commit in period) {
                markedCommits[commit.name] = i
            }
        }
    }

}
