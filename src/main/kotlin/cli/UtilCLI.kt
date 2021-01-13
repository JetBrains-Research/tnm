package cli

import gitMiners.UtilGitMiner
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import util.ProjectConfig

object UtilCLI {

    const val helpResourcesOpt = "Directory where to store all results"
    const val helpNumThreads = "Number of working threads for task. By default ${ProjectConfig.numThreads}"

    fun checkBranchesArgsMsg(repository: FileRepository?): String {
        repository ?: return ""

        val branchesShortNames = UtilGitMiner.getBranchesShortNames(Git(repository))
        return buildString {
            appendLine("Chose from following branches:")
            branchesShortNames.forEach { appendLine(it) }
        }
    }

}
