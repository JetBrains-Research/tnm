package cli.gitMinersCLI

import cli.InfoCLI
import gitMiners.WorkTimeMiner
import util.ProjectConfig

class WorkTimeMinerCLI : MultithreadedGitMinerCLI(
    InfoCLI(
        "WorkTimeMiner",
        "Mine commits time distribution for each developer. Result is matrix where row represents user, column " +
                "represents minute in week and value in cell represent number of commits. Output is JSON file named as ${ProjectConfig.WORKTIME_DISTRIBUTION}"
    )
) {
    override fun run() {
        val miner = WorkTimeMiner(repository, branches, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }
}