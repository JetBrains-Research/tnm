package cli.gitMinersCLI.base

import cli.InfoCLI
import cli.UtilCLI
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import util.ProjectConfig

abstract class GitMinerMultithreadedMultipleBranchesCLI(infoCLI: InfoCLI) : GitMinerMultipleBranchesCLI(infoCLI) {
    protected val numThreads by option(
        UtilCLI.SHORTNAME_NUM_THREADS,
        UtilCLI.LONGNAME_NUM_THREADS,
        help = UtilCLI.HELP_NUM_THREADS
    )
        .int()
        .default(ProjectConfig.DEFAULT_NUM_THREADS)
}