package cli.gitMinersCLI

import cli.InfoCLI
import cli.gitMinersCLI.base.GitMinerMultithreadedMultipleBranchesCLI
import gitMiners.AssignmentMatrixMiner
import util.ProjectConfig

class AssignmentMatrixMinerCLI :
    GitMinerMultithreadedMultipleBranchesCLI
        (
        InfoCLI(
            "AssignmentMatrixMiner",
            "Miner yields a JSON file ${ProjectConfig.ASSIGNMENT_MATRIX} with map of maps, where outer " +
                    "key is the user id, inner key is the file id and the value is the number of times the user has edited the file."
        )
    ) {

    override fun run() {
        val miner = AssignmentMatrixMiner(repository, branches, numThreads = numThreads)
//        miner.run()
//        miner.saveToJson(resources)
    }
}