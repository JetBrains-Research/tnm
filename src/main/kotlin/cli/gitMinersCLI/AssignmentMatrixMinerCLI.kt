package cli.gitMinersCLI

import cli.InfoCLI
import gitMiners.AssignmentMatrixMiner

class AssignmentMatrixMinerCLI :
    GitMinerCLI(InfoCLI("AssignmentMatrixMiner", "Mine the assignments of people to a technical entities")) {
    override fun run() {
        val miner = AssignmentMatrixMiner(repository!!, branches)
        miner.run()
        miner.saveToJson(resources!!)
    }
}