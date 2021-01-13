package cli.gitMinersCLI

import cli.InfoCLI
import gitMiners.ChangedFilesMiner
import util.ProjectConfig

class ChangedFilesMinerCLI : MultithreadedGitMinerCLI(
    InfoCLI(
        "ChangedFilesMiner",
        "Mine set of changed files by each developer. Result is map of sets where key represents developer and " +
                "set contains changed files. Output is JSON file named as ${ProjectConfig.USER_FILES_IDS}"
    )
) {
    override fun run() {
        val miner = ChangedFilesMiner(repository, branches, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }
}