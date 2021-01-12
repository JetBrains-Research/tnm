package cli.gitMinersCLI

import cli.InfoCLI
import cli.UtilCLI
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import gitMiners.AssignmentMatrixMiner
import util.ProjectConfig

// TODO: add multithreading abstract miner class
class AssignmentMatrixMinerCLI :
    GitMinerCLI(InfoCLI("AssignmentMatrixMiner", "Mine the assignments of people to a technical entities")) {
    private val numThreads by option("-n", "--num-threads", help = UtilCLI.helpNumThreads)
        .int()
        .default(ProjectConfig.numThreads)

    override fun run() {
        val miner = AssignmentMatrixMiner(repository!!, branches, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources!!)
    }
}