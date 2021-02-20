package cli.gitMinersCLI.base

import cli.InfoCLI
import cli.UtilCLI
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import gitMiners.UtilGitMiner
import org.eclipse.jgit.api.Git

abstract class GitMinerOneBranchCLI(infoCLI: InfoCLI) :
    GitMinerCLI(infoCLI) {
    protected val branch by argument(help = UtilCLI.HELP_ONE_BRANCH)
        .validate {
            require(it in UtilGitMiner.getBranchesShortNames(Git(repository))) {
                UtilCLI.checkBranchesArgsMsg(
                    repository
                )
            }
        }
}