package cli.gitMinersCLI

import cli.InfoCLI
import cli.UtilCLI
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.unique
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import gitMiners.UtilGitMiner
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository

abstract class GitMinerCLI(infoCLI: InfoCLI) :
    CliktCommand(name = infoCLI.name, help = infoCLI.help) {

    protected val repository by option("--repository", help = "Git repository directory")
        .file(mustExist = true, canBeDir = true, canBeFile = false)
        .convert { FileRepository(it) }
        .required()
        .check("is not git repository ") { !it.isBare }

    protected val resources by option("--resources", help = UtilCLI.helpResourcesOpt)
        .file(mustExist = true, canBeDir = true, canBeFile = false)
        .required()

    protected val branches by argument(help = "Set of branches which need to be proceeded ")
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
