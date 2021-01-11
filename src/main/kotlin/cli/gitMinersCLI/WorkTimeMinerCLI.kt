package cli.gitMinersCLI

import cli.InfoCLI
import gitMiners.WorkTimeMiner

class WorkTimeMinerCLI : GitMinerCLI(
    InfoCLI("WorkTimeMiner", "Mine commits time distribution for each developer")
) {
    override fun run() {
        val miner = WorkTimeMiner(repository!!, branches)
        miner.run()
        miner.saveToJson(resources!!)
    }
}