package cli.gitMinersCLI

import cli.InfoCLI
import cli.gitMinersCLI.base.GitMinerMultithreadedOneBranchCLI
import util.ProjectConfig

class ComplexityCodeChangesCLI : GitMinerMultithreadedOneBranchCLI(
    InfoCLI(
        "ComplexityCodeChangesMiner",
        "Miner yields JSON file ${ProjectConfig.COMPLEXITY_CODE} with dict of periods, which got period's entropy and " +
                "files (changed in that period) stats. Each file stat includes entropy and History Complexity Period Factors, such as " +
                "HCPF2 and HCPF3."
    )
) {

//    private val numOfMonth by option(
//        "--num-of-month",
//        help = "Number of month in one period. Used in creating period of ${PeriodType.TIME_BASED} type. " +
//                "By default ${ComplexityCodeChangesMiner.DEFAULT_NUM_MONTH}. " +
//                "Period starts from latest commit in branch."
//    )
//        .int()
//        .default(ComplexityCodeChangesMiner.DEFAULT_NUM_MONTH)
//
//    private val numOfCommits by option(
//        "--num-of-commits",
//        help = "Number of commits in one period. Used in creating period of ${PeriodType.MODIFICATION_LIMIT} type. " +
//                "By default ${ComplexityCodeChangesMiner.DEFAULT_NUM_COMMITS}. " +
//                "Period starts from latest commit in branch."
//    )
//        .int()
//        .default(ComplexityCodeChangesMiner.DEFAULT_NUM_COMMITS)
//
//    private val changeType by option(
//        "--change-type",
//        help = "Mine statistic based on specified type of changes. " +
//                "By default ${ComplexityCodeChangesMiner.DEFAULT_CHANGE_TYPE}. Possible values " +
//                "${ChangeType.values().map { it.toString() }}"
//    )
//        .default(ComplexityCodeChangesMiner.DEFAULT_CHANGE_TYPE.toString())
//        .validate { type ->
//            require(type in ChangeType.values().map { it.toString() }) {
//                "Possible values for change type are ${ChangeType.values().map { it.toString() }}"
//            }
//        }
//
//    private val periodType by option(
//        "--period-type",
//        help = "Mine statistic based on specified type of periods. " +
//                "By default ${ComplexityCodeChangesMiner.DEFAULT_PERIOD_TYPE}. Possible values " +
//                "${PeriodType.values().map { it.toString() }}"
//    )
//        .default(ComplexityCodeChangesMiner.DEFAULT_PERIOD_TYPE.toString())
//        .validate { type ->
//            require(type in PeriodType.values().map { it.toString() }) {
//                "Possible values for period type are ${ChangeType.values().map { it.toString() }}"
//            }
//        }
//
//    private val changedTypeStringToEnum = mutableMapOf<String, ChangeType>()
//    private val periodTypeStringToEnum = mutableMapOf<String, PeriodType>()

//    init {
//        for (type in ChangeType.values()) {
//            changedTypeStringToEnum[type.toString()] = type
//        }
//
//        for (type in PeriodType.values()) {
//            periodTypeStringToEnum[type.toString()] = type
//        }
//    }

    override fun run() {
//        val miner = ComplexityCodeChangesMiner(
//            repository,
//            branch,
//            numThreads = numThreads,
//            numOfMonthInPeriod = numOfMonth,
//            numOfCommitsInPeriod = numOfCommits,
//            changeType = changedTypeStringToEnum[changeType]!!,
//            periodType = periodTypeStringToEnum[periodType]!!
//        )
//        miner.run()
//        miner.saveToJson(resources)
    }
}