package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.check
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.unique
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import gitMiners.GitMiner
import gitMiners.UtilGitMiner
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class GitMinerCLI(private val clazz: KClass<out GitMiner>, name: String, help: String) :
    CliktCommand(name = name, help = help) {

    private val repository by option("--repository", help = UtilCLI.helpRepositoryOpt)
        .file(mustExist = true, canBeDir = true, canBeFile = false)
        .convert { FileRepository(it) }
        .check("is not git repository ") { !it.isBare }

    private val resources by option("--resources", help = UtilCLI.helpResourcesOpt)
        .file(mustExist = true, canBeDir = true, canBeFile = false)

    private val branches by argument("-b", "-branches")
        .multiple()
        .unique()
        .check(UtilCLI.checkBranchesArgs(repository)) {
            repository ?: return@check false
            (it - UtilGitMiner.getAvailableBranchesShortNames(Git(repository))).isEmpty()
        }


    override fun run() {
        val constructor = clazz.primaryConstructor
        val miner = constructor!!.call(repository, branches)
        miner.run()
        miner.saveToJson(resources!!)
    }
}

