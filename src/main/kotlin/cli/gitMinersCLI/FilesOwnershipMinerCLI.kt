package cli.gitMinersCLI

import cli.InfoCLI
import gitMiners.FilesOwnershipMiner

class FilesOwnershipMinerCLI : GitMinerCLI(
    InfoCLI(
        "FilesOwnershipMiner",
        "Mine developers knowledge for each file based on Degree of knowledge algorithm. Result is " +
                "2 matrices of FileOwnership[fileId][userId], DeveloperKnowledge[userId][fileId] and " +
                "list of PotentialAuthorship[fileId] for each file. Output is JSON files."
    )
) {
    override fun run() {
        val miner = FilesOwnershipMiner(repository, branches)
        miner.run()
        miner.saveToJson(resources)
    }
}