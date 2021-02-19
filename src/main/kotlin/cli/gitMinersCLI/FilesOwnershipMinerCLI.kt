package cli.gitMinersCLI

import cli.InfoCLI
import cli.gitMinersCLI.base.GitMinerMultithreadedOneBranchCLI
import gitMiners.FilesOwnershipMiner
import util.ProjectConfig

class FilesOwnershipMinerCLI : GitMinerMultithreadedOneBranchCLI(
    InfoCLI(
        "FilesOwnershipMiner",
        "Miner yields JSON file ${ProjectConfig.DEVELOPER_KNOWLEDGE} with map of maps, where the outer " +
                "key is the user id, the inner key is the file id and the value is the developer knowledge about the file."
    )
) {

    override fun run() {
        val miner = FilesOwnershipMiner(repository, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }
}