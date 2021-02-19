package cli.gitMinersCLI.base

import cli.InfoCLI
import cli.UtilCLI
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.unique
import com.github.ajalt.clikt.parameters.arguments.validate
import gitMiners.UtilGitMiner
import org.eclipse.jgit.api.Git

abstract class GitMinerMultipleBranchesCLI(infoCLI: InfoCLI) :
    GitMinerCLI(infoCLI) {
    protected val branches by argument(help = UtilCLI.HELP_MULTIPLE_BRANCHES)
        .multiple()
        .unique()
        .validate {
            require((it - UtilGitMiner.getBranchesShortNames(Git(repository))).isEmpty()) {
                UtilCLI.checkBranchesArgsMsg(
                    repository
                )
            }
        }
}
