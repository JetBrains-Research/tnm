package cli.gitMinersCLI

import cli.InfoCLI
import gitMiners.FileDependencyMatrixMiner

class FileDependencyMatrixMinerCLI :
    MultithreadedGitMinerCLI(
        InfoCLI("FileDependencyMatrixMiner", "Mine technical dependencies based on commits")
    ) {

    override fun run() {
        val miner = FileDependencyMatrixMiner(repository, branches, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }
}