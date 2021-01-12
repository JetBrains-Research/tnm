package cli.gitMinersCLI

import cli.InfoCLI
import gitMiners.ChangedFilesMiner

class ChangedFilesMinerCLI : MultithreadedGitMinerCLI(
    InfoCLI(
        "ChangedFilesMiner",
        "Mine the number of the common edited files in a project between developers"
    )
) {
    override fun run() {
        val miner = ChangedFilesMiner(repository!!, branches, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources!!)
    }
}