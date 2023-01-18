package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.unique
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import miners.gitMiners.GitMinerUtil
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.Repository
import util.HelpFunctionsUtil
import util.ProjectConfig
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

        fun checkBranchesArgsMsg(repository: Repository?): String {
            repository ?: return ""

            val branchesShortNames = GitMinerUtil.getBranchesShortNames(Git(repository))
            return buildString {
                appendLine("Chose from following branches:")
                branchesShortNames.forEach { appendLine(it) }
            }
        }

        private fun mapperHelp(from: String, to: String) =
            "Json file of map of $from to unique $to. Not all miners use it."

    }

    protected val resultDir = File("./result")


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


    protected fun loadFileOption(longname: String, help: String): OptionWithValues<File, File, File> {
        return option(
            longname,
            help = help
        )
            .file(mustExist = true, canBeDir = false, canBeFile = true)
            .required()
    }

    protected fun idToFileOption() =
        saveFileOption(
            LONGNAME_ID_TO_FILE,
            mapperHelp("id", "file"), File(resultDir, "idToFile")
        )

    protected fun fileToIdOption() =
        saveFileOption(
            LONGNAME_FILE_TO_ID,
            mapperHelp("file", "id"), File(resultDir, "fileToId")
        )

    protected fun idToUserOption() =
        saveFileOption(
            LONGNAME_ID_TO_USER,
            mapperHelp("id", "user"), File(resultDir, "idToUser")
        )

    protected fun userToIdOption() =
        saveFileOption(
            LONGNAME_USER_TO_ID,
            mapperHelp("user", "id"), File(resultDir, "userToId")
        )

    protected fun idToCommitOption() =
        saveFileOption(
            LONGNAME_ID_TO_COMMIT,
            mapperHelp("id", "commit"), File(resultDir, "idToCommit")
        )

    protected fun commitToIdOption() =
        saveFileOption(
            LONGNAME_COMMIT_TO_ID,
            mapperHelp("commit", "id"), File(resultDir, "commitToId")
        )

    protected fun numOfThreadsOption() = option(
        SHORTNAME_NUM_THREADS,
        LONGNAME_NUM_THREADS,
        help = HELP_NUM_THREADS
    )
        .int()
        .default(ProjectConfig.DEFAULT_NUM_THREADS)

}