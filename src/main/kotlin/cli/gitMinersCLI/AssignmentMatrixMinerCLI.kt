package cli.gitMinersCLI

import cli.InfoCLI
import gitMiners.AssignmentMatrixMiner
import util.ProjectConfig

class AssignmentMatrixMinerCLI :
    MultithreadedGitMinerCLI
        (
        InfoCLI(
            "AssignmentMatrixMiner",
            "Mine the assignments of people to a technical entities. Result is matrix " +
                    "where row represents developer, column represents file and value in cell represents how many " +
                    "times developer changed file. Output is JSON file named as ${ProjectConfig.ASSIGNMENT_MATRIX}"
        )
    ) {

    override fun run() {
        val miner = AssignmentMatrixMiner(repository, branches, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources)
    }
}