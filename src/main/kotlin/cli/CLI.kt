package cli
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.*
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import gitMiners.*
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import kotlin.reflect.KClass

// TODO: autocomplition
// TODO: println -> echo ?

class CLI : CliktCommand() {
    companion object {
        fun createGitMinerCLI(clazz: KClass<out GitMiner>, help: String): GitMinerCLI {
            return GitMinerCLI(clazz, clazz.simpleName!!, help)
        }
    }
//    private val graphName by option("-g", "--graph-name", help = UtilCLI.helpGraphNameOpt)
    override fun run() {}
}

fun main(args: Array<String>) = CLI().subcommands(
    CLI.createGitMinerCLI(AssignmentMatrixMiner::class, ""),
    CLI.createGitMinerCLI(ChangedFilesMiner::class, ""),
    CLI.createGitMinerCLI(FileDependencyMatrixMiner::class, ""),
    CLI.createGitMinerCLI(FilesOwnershipMiner::class, ""),
    CLI.createGitMinerCLI(PageRankMiner::class, ""),
    CLI.createGitMinerCLI(WorkTimeMiner::class, "")
).main(args)
