package cli.gitMinersCLI

import cli.InfoCLI
import gitMiners.FilesOwnershipMiner

class FilesOwnershipMinerCLI : GitMinerCLI(
    InfoCLI(
        "FilesOwnershipMiner",
        "Mine developers knowledge for each file based on Degree of knowledge algorithm"
    )
) {
    override fun run() {
        val miner = FilesOwnershipMiner(repository, branches)
        miner.run()
        miner.saveToJson(resources)
    }
}