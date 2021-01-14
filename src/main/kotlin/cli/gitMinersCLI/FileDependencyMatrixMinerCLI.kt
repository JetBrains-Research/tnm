package cli.gitMinersCLI

import cli.InfoCLI
import gitMiners.FileDependencyMatrixMiner
import util.ProjectConfig

class FileDependencyMatrixMinerCLI :
    MultithreadedGitMinerCLI(
        InfoCLI(
            "FileDependencyMatrixMiner",
            "Miner yields a JSON file ${ProjectConfig.FILE_DEPENDENCY} with map of maps, where both inner and outer " +
                    "keys are file ids and the value is the number of times both file has been edited in the same commit."
        )
    ) {

    override fun run() {
        val miner = FileDependencyMatrixMiner(repository, branches, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }
}