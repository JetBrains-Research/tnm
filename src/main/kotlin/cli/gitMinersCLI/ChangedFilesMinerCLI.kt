package cli.gitMinersCLI

import cli.InfoCLI
import gitMiners.ChangedFilesMiner

class ChangedFilesMinerCLI : GitMinerCLI(
    InfoCLI(
        "ChangedFilesMiner",
        "Mine the number of the common edited files in a project between developers"
    )
) {
    override fun run() {
        val miner = ChangedFilesMiner(repository!!, branches)
        miner.run()
        miner.saveToJson(resources!!)
    }
}