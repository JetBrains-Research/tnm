package cli.gitMinersCLI

import cli.InfoCLI
import gitMiners.FileDependencyMatrixMiner

class FileDependencyMatrixMinerCLI :
    GitMinerCLI(InfoCLI("FileDependencyMatrixMiner", "Mine technical dependencies based on commits")) {
    override fun run() {
        val miner = FileDependencyMatrixMiner(repository!!, branches)
        miner.run()
        miner.saveToJson(resources!!)
    }
}