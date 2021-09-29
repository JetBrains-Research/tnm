package cli.gitMinersCLI

import cli.gitMinersCLI.base.GitMinerMultithreadedOneBranchCLI
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import dataProcessor.ComplexityCodeChangesDataProcessor
import dataProcessor.ComplexityCodeChangesDataProcessor.ChangeType
import dataProcessor.ComplexityCodeChangesDataProcessor.Companion.DEFAULT_CHANGE_TYPE
import dataProcessor.ComplexityCodeChangesDataProcessor.Companion.DEFAULT_NUM_COMMITS
import dataProcessor.ComplexityCodeChangesDataProcessor.Companion.DEFAULT_NUM_MONTH
import dataProcessor.ComplexityCodeChangesDataProcessor.Companion.DEFAULT_PERIOD_TYPE
import dataProcessor.ComplexityCodeChangesDataProcessor.PeriodType
import miners.gitMiners.ComplexityCodeChangesMiner
import util.HelpFunctionsUtil
import java.io.File

class ComplexityCodeChangesCLI : GitMinerMultithreadedOneBranchCLI(
    "ComplexityCodeChangesMiner",
    "Miner yields $HELP_COMPLEXITY_CODE_CHANGES"
) {

    companion object {
        const val HELP_COMPLEXITY_CODE_CHANGES = "JSON file with dict of periods, which got period's entropy and " +
                "files (changed in that period) stats. Each file stat includes entropy and History Complexity Period Factors, such as " +
                "HCPF2 and HCPF3."

        const val LONGNAME_NUM_OF_MONTH = "--num-of-month"
        const val LONGNAME_NUM_OF_COMMITS = "--num-of-commits"
        const val LONGNAME_CHANGE_TYPE = "--change-type"
        const val LONGNAME_PERIOD_TYPE = "--period-type"
        const val LONGNAME_COMPLEXITY_CODE_CHANGES = "--complexity-code-changes"
    }

    private val numOfMonth by option(
        LONGNAME_NUM_OF_MONTH,
        help = "Number of month in one period. Used in creating period of ${PeriodType.TIME_BASED} type. " +
                "By default ${DEFAULT_NUM_MONTH}. " +
                "Period starts from latest commit in branch."
    )
        .int()
        .default(DEFAULT_NUM_MONTH)

    private val numOfCommits by option(
        LONGNAME_NUM_OF_COMMITS,
        help = "Number of commits in one period. Used in creating period of ${PeriodType.MODIFICATION_LIMIT} type. " +
                "By default ${DEFAULT_NUM_COMMITS}. " +
                "Period starts from latest commit in branch."
    )
        .int()
        .default(DEFAULT_NUM_COMMITS)

    private val changeType by option(
        LONGNAME_CHANGE_TYPE,
        help = "Mine statistic based on specified type of changes. " +
                "By default ${DEFAULT_CHANGE_TYPE}. Possible values " +
                "${ChangeType.values().map { it.toString() }}"
    )
        .default(DEFAULT_CHANGE_TYPE.toString())
        .validate { type ->
            require(type in ChangeType.values().map { it.toString() }) {
                "Possible values for change type are ${ChangeType.values().map { it.toString() }}"
            }
        }

    private val periodType by option(
        LONGNAME_PERIOD_TYPE,
        help = "Mine statistic based on specified type of periods. " +
                "By default ${DEFAULT_PERIOD_TYPE}. Possible values " +
                "${PeriodType.values().map { it.toString() }}"
    )
        .default(DEFAULT_PERIOD_TYPE.toString())
        .validate { type ->
            require(type in PeriodType.values().map { it.toString() }) {
                "Possible values for period type are ${ChangeType.values().map { it.toString() }}"
            }
        }

    private val changedTypeStringToEnum = mutableMapOf<String, ChangeType>()
    private val periodTypeStringToEnum = mutableMapOf<String, PeriodType>()

    init {
        for (type in ChangeType.values()) {
            changedTypeStringToEnum[type.toString()] = type
        }

        for (type in PeriodType.values()) {
            periodTypeStringToEnum[type.toString()] = type
        }
    }


    private val complexityCodeChangesJsonFile by saveFileOption(
        LONGNAME_COMPLEXITY_CODE_CHANGES,
        HELP_COMPLEXITY_CODE_CHANGES,
        File(resultDir, "ComplexityCodeChanges")
    )

    private val idToFileJsonFile by idToFileOption()

    override fun run() {
        val dataProcessor = ComplexityCodeChangesDataProcessor(
            numOfMonthInPeriod = numOfMonth,
            numOfCommitsInPeriod = numOfCommits,
            changeType = changedTypeStringToEnum[changeType]!!,
            periodType = periodTypeStringToEnum[periodType]!!
        )

        val miner = ComplexityCodeChangesMiner(repositoryDirectory, branch, numThreads = numThreads)
        miner.run(dataProcessor)

        HelpFunctionsUtil.saveToJson(
            complexityCodeChangesJsonFile,
            dataProcessor.periodsToStats
        )

        HelpFunctionsUtil.saveToJson(
            idToFileJsonFile,
            dataProcessor.idToFile
        )
    }
}