package cli.gitMinersCLI

import cli.InfoCLI
import cli.UtilCLI
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import util.ProjectConfig

abstract class MultithreadedGitMinerCLI(infoCLI: InfoCLI) : GitMinerCLI(infoCLI) {
    protected val numThreads by option("-n", "--num-threads", help = UtilCLI.helpNumThreads)
        .int()
        .default(ProjectConfig.DEFAULT_NUM_THREADS)
}