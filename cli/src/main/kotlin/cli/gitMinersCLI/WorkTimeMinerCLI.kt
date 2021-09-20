package cli.gitMinersCLI

import cli.gitMinersCLI.base.GitMinerMultithreadedMultipleBranchesCLI
import dataProcessor.WorkTimeDataProcessor
import miners.gitMiners.WorkTimeMiner
import org.eclipse.jgit.internal.storage.file.FileRepository
import util.HelpFunctionsUtil
import java.io.File

class WorkTimeMinerCLI : GitMinerMultithreadedMultipleBranchesCLI(
    "WorkTimeMiner",
    "Miner yields a $HELP_WORK_TIME."
) {

    companion object {
        const val HELP_WORK_TIME = "JSON file with a map of maps, where the outer " +
                "key is user id, the inner key is the minutes passed from the beginning of the week, and the value is " +
                "the number of commits made by user at that minute in week. The first day of the week is SUNDAY"
        const val LONGNAME_WORK_TIME = "--work-time"
    }

    private val workTimeJsonFile by saveFileOption(
        LONGNAME_WORK_TIME,
        HELP_WORK_TIME,
        File(resultDir, "WorkTime")
    )

    private val idToUserJsonFile by idToUserOption()

    override fun run() {
        val dataProcessor = WorkTimeDataProcessor()
        val miner = WorkTimeMiner(repositoryDirectory, branches, numThreads = numThreads)
        miner.run(dataProcessor)

        HelpFunctionsUtil.saveToJson(
            workTimeJsonFile,
            dataProcessor.workTimeDistribution
        )

        HelpFunctionsUtil.saveToJson(
            idToUserJsonFile,
            dataProcessor.idToUser
        )
    }
}