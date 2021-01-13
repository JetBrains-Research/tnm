package cli.gitMinersCLI

import cli.InfoCLI
import gitMiners.FileDependencyMatrixMiner
import util.ProjectConfig

class FileDependencyMatrixMinerCLI :
    MultithreadedGitMinerCLI(
        InfoCLI(
            "FileDependencyMatrixMiner",
            "Mine technical dependencies based on commits. Result is matrix where row, column " +
                    "represents file and value in cell represents how many times files occur in same changed files " +
                    "set of commit. Output is JSON file named as ${ProjectConfig.FILE_DEPENDENCY}"
        )
    ) {

    override fun run() {
        val miner = FileDependencyMatrixMiner(repository, branches, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }
}