package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.unique
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import miners.gitMiners.UtilGitMiner
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import util.ProjectConfig
import util.UtilFunctions
import java.io.File

abstract class AbstractCLI(name: String, help: String) :
    CliktCommand(name = name, help = help) {

    companion object {
        const val LONGNAME_ID_TO_FILE = "--id-to-file"
        const val LONGNAME_FILE_TO_ID = "--file-to-id"

        const val LONGNAME_ID_TO_USER = "--id-to-user"
        const val LONGNAME_USER_TO_ID = "--user-to-id"

        const val LONGNAME_ID_TO_COMMIT = "--id-to-commit"
        const val LONGNAME_COMMIT_TO_ID = "--commit-to-id"

        const val LONGNAME_REPOSITORY = "--repository"
        const val HELP_REPOSITORY = "Git repository directory"
        const val ERR_NOT_GIT_REPO =
            "Passed value for repository is not git repository. Maybe you forgot to add '.git'?"

        val HELP_NUM_THREADS =
            "Number of working threads for task. By default number of cores of your processor. Found: ${ProjectConfig.DEFAULT_NUM_THREADS}"
        const val SHORTNAME_NUM_THREADS = "-n"
        const val LONGNAME_NUM_THREADS = "--num-threads"

        const val HELP_ONE_BRANCH = "Branch which need to be proceeded "
        const val HELP_MULTIPLE_BRANCHES = "Set of branches which need to be proceeded "

        fun checkBranchesArgsMsg(repository: FileRepository?): String {
            repository ?: return ""

            val branchesShortNames = UtilGitMiner.getBranchesShortNames(Git(repository))
            return buildString {
                appendLine("Chose from following branches:")
                branchesShortNames.forEach { appendLine(it) }
            }
        }

        private fun mapperHelp(from: String, to: String) =
            "Json file of map of $from to unique $to. Not all miners use it."

    }

    protected val resultDir = File("./result")

    protected val repositoryDirectory by option(LONGNAME_REPOSITORY, help = HELP_REPOSITORY)
        .file(mustExist = true, canBeDir = true, canBeFile = false)
        .required()
        .check(ERR_NOT_GIT_REPO) { UtilFunctions.isGitRepository(it) }

    protected fun saveFileOption(
        longname: String,
        help: String,
        defaultFile: File
    ) = option(
        longname,
        help = help + " Default value is ${defaultFile.path}."
    )
        .file(mustExist = false, canBeDir = false, canBeFile = true)
        .default(defaultFile)


    protected fun loadFileOption(longname: String, help: String): NullableOption<File, File> {
        return option(
            longname,
            help = help
        )
            .file(mustExist = true, canBeDir = false, canBeFile = true)
    }

    protected fun idToFileOption() =
        saveFileOption(
            cli.AbstractCLI.Companion.LONGNAME_ID_TO_FILE,
            cli.AbstractCLI.Companion.mapperHelp("id", "file"), File(resultDir, "idToFile")
        )

    protected fun fileToIdOption() =
        saveFileOption(
            cli.AbstractCLI.Companion.LONGNAME_FILE_TO_ID,
            cli.AbstractCLI.Companion.mapperHelp("file", "id"), File(resultDir, "fileToId")
        )

    protected fun idToUserOption() =
        saveFileOption(
            cli.AbstractCLI.Companion.LONGNAME_ID_TO_USER,
            cli.AbstractCLI.Companion.mapperHelp("id", "user"), File(resultDir, "idToUser")
        )

    protected fun userToIdOption() =
        saveFileOption(
            cli.AbstractCLI.Companion.LONGNAME_USER_TO_ID,
            cli.AbstractCLI.Companion.mapperHelp("user", "id"), File(resultDir, "userToId")
        )

    protected fun idToCommitOption() =
        saveFileOption(
            cli.AbstractCLI.Companion.LONGNAME_ID_TO_COMMIT,
            cli.AbstractCLI.Companion.mapperHelp("id", "commit"), File(resultDir, "idToCommit")
        )

    protected fun commitToIdOption() =
        saveFileOption(
            cli.AbstractCLI.Companion.LONGNAME_COMMIT_TO_ID,
            cli.AbstractCLI.Companion.mapperHelp("commit", "id"), File(resultDir, "commitToId")
        )

    protected fun branchesOption() = argument(help = cli.AbstractCLI.Companion.HELP_MULTIPLE_BRANCHES)
        .multiple()
        .unique()
        .validate {
            require((it - UtilGitMiner.getBranchesShortNames(Git(FileRepository(repositoryDirectory)))).isEmpty()) {
                cli.AbstractCLI.Companion.checkBranchesArgsMsg(
                    FileRepository(repositoryDirectory)
                )
            }
        }

    protected fun oneBranchOption() = argument(help = cli.AbstractCLI.Companion.HELP_ONE_BRANCH)
        .validate {
            require(it in UtilGitMiner.getBranchesShortNames(Git(FileRepository(repositoryDirectory)))) {
                cli.AbstractCLI.Companion.checkBranchesArgsMsg(
                    FileRepository(repositoryDirectory)
                )
            }
        }

    protected fun numOfThreadsOption() = option(
        cli.AbstractCLI.Companion.SHORTNAME_NUM_THREADS,
        cli.AbstractCLI.Companion.LONGNAME_NUM_THREADS,
        help = cli.AbstractCLI.Companion.HELP_NUM_THREADS
    )
        .int()
        .default(ProjectConfig.DEFAULT_NUM_THREADS)

}