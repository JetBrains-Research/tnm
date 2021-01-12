package cli.gitMinersCLI

import cli.InfoCLI
import gitMiners.WorkTimeMiner

class WorkTimeMinerCLI : MultithreadedGitMinerCLI(
    InfoCLI("WorkTimeMiner", "Mine commits time distribution for each developer")
) {
    override fun run() {
        val miner = WorkTimeMiner(repository, branches, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }
}