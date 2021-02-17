package cli

import gitMiners.UtilGitMiner
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import util.ProjectConfig

object UtilCLI {

    const val helpResourcesOpt = "Directory where to store all results"
    val helpNumThreads =
        "Number of working threads for task. By default number of cores of your processor. Found: ${ProjectConfig.DEFAULT_NUM_THREADS}"

    fun checkBranchesArgsMsg(repository: FileRepository?): String {
        repository ?: return ""

        val branchesShortNames = UtilGitMiner.getBranchesShortNames(Git(repository))
        return buildString {
            appendLine("Chose from following branches:")
            branchesShortNames.forEach { appendLine(it) }
        }
    }

}
