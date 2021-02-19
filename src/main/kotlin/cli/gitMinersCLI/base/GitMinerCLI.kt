package cli.gitMinersCLI.base

import cli.InfoCLI
import cli.UtilCLI
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import org.eclipse.jgit.internal.storage.file.FileRepository

abstract class GitMinerCLI(infoCLI: InfoCLI) :
    CliktCommand(name = infoCLI.name, help = infoCLI.help) {

    protected val repository by option(UtilCLI.LONGNAME_REPOSITORY, help = UtilCLI.HELP_REPOSITORY_OPT)
        .file(mustExist = true, canBeDir = true, canBeFile = false)
        .convert { FileRepository(it) }
        .required()
        .check(UtilCLI.ERR_NOT_GIT_REPO) { !it.isBare }

    protected val resources by option(UtilCLI.LONGNAME_RESOURCES, help = UtilCLI.HELP_RESOURCES_OPT)
        .file(mustExist = true, canBeDir = true, canBeFile = false)
        .required()
}
