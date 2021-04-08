package cli

import miners.gitMiners.UtilGitMiner
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import util.ProjectConfig

object UtilCLI {

    // Commonly used options

    // repository option
    const val LONGNAME_REPOSITORY = "--repository"
    const val HELP_REPOSITORY_OPT = "Git repository directory"
    const val ERR_NOT_GIT_REPO = "is not git repository "

    // resources option
    const val LONGNAME_RESOURCES = "--resources"
    const val HELP_RESOURCES_OPT = "Directory where all results stored"

    // number of threads option
    val HELP_NUM_THREADS =
        "Number of working threads for task. By default number of cores of your processor. Found: ${ProjectConfig.DEFAULT_NUM_THREADS}"
    const val SHORTNAME_NUM_THREADS = "-n"
    const val LONGNAME_NUM_THREADS = "--num-threads"

    // one branch option
    const val HELP_ONE_BRANCH = "Branch which need to be proceeded "

    // multiple branch option
    const val HELP_MULTIPLE_BRANCHES = "Set of branches which need to be proceeded "

    fun checkBranchesArgsMsg(repository: FileRepository?): String {
        repository ?: return ""

        val branchesShortNames = UtilGitMiner.getBranchesShortNames(Git(repository))
        return buildString {
            appendLine("Chose from following branches:")
            branchesShortNames.forEach { appendLine(it) }
        }
    }

}
