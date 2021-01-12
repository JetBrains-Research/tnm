package cli.gitMinersCLI

import cli.InfoCLI
import gitMiners.AssignmentMatrixMiner

class AssignmentMatrixMinerCLI :
    MultithreadedGitMinerCLI
        (InfoCLI("AssignmentMatrixMiner", "Mine the assignments of people to a technical entities")) {

    override fun run() {
        val miner = AssignmentMatrixMiner(repository!!, branches, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources!!)
    }
}