package cli.gitMinersCLI

import cli.InfoCLI
import gitMiners.FilesOwnershipMiner
import util.ProjectConfig

class FilesOwnershipMinerCLI : GitMinerCLI(
    InfoCLI(
        "FilesOwnershipMiner",
        "Miner yields JSON file ${ProjectConfig.DEVELOPER_KNOWLEDGE} with map of maps, where the outer " +
                "key is the user id, the inner key is the file id and the value is the developer knowledge about the file."
    )
) {
    override fun run() {
        val miner = FilesOwnershipMiner(repository, branches)
        miner.run()
        miner.saveToJson(resources)
    }
}