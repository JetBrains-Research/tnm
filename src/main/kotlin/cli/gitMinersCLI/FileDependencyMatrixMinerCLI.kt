package cli.gitMinersCLI

import cli.InfoCLI
import cli.UtilCLI
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import gitMiners.FileDependencyMatrixMiner
import util.ProjectConfig

// TODO: add multithreading abstract miner class
class FileDependencyMatrixMinerCLI :
    GitMinerCLI(InfoCLI("FileDependencyMatrixMiner", "Mine technical dependencies based on commits")) {
    private val numThreads by option("-n", "--num-threads", help = UtilCLI.helpNumThreads)
        .int()
        .default(ProjectConfig.numThreads)

    override fun run() {
        val miner = FileDependencyMatrixMiner(repository!!, branches, numThreads = numThreads)
        miner.run()
        miner.saveToJson(resources!!)
    }
}