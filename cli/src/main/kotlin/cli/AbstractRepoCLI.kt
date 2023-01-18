package cli

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.unique
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import miners.gitMiners.GitMinerUtil
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import util.HelpFunctionsUtil

abstract class AbstractRepoCLI(name: String, help: String): AbstractCLI(name, help) {

  companion object {
    const val HELP_ONE_BRANCH = "Branch which need to be proceeded "
    const val HELP_MULTIPLE_BRANCHES = "Set of branches which need to be proceeded "

  }

  protected val repositoryDirectory by option(LONGNAME_REPOSITORY, help = HELP_REPOSITORY)
    .file(mustExist = true, canBeDir = true, canBeFile = false)
    .required()
    .check(ERR_NOT_GIT_REPO) { HelpFunctionsUtil.isGitRepository(it) }

  protected fun branchesOption() = argument(help = HELP_MULTIPLE_BRANCHES)
    .multiple()
    .unique()
    .validate {
      require((it - GitMinerUtil.getBranchesShortNames(Git(FileRepository(repositoryDirectory)))).isEmpty()) {
        checkBranchesArgsMsg(
          FileRepository(repositoryDirectory)
        )
      }
    }

  protected fun oneBranchOption() = argument(help = HELP_ONE_BRANCH)
    .validate {
      require(it in GitMinerUtil.getBranchesShortNames(Git(FileRepository(repositoryDirectory)))) {
        checkBranchesArgsMsg(
          FileRepository(repositoryDirectory)
        )
      }
    }
}