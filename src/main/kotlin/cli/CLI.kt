package cli

import cli.calculculationsCLI.CNMatrixCalculationCLI
import cli.calculculationsCLI.PageRankCalculationCLI
import cli.gitMinersCLI.*
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

// TODO: autocomplition
// TODO: println -> echo ?

class CLI : CliktCommand() {
    //    private val graphName by option("-g", "--graph-name", help = UtilCLI.helpGraphNameOpt)
    override fun run() {}
}

fun main(args: Array<String>) = CLI().subcommands(
    AssignmentMatrixMinerCLI(),
    ChangedFilesMinerCLI(),
    FileDependencyMatrixMinerCLI(),
    FilesOwnershipMinerCLI(),
    PageRankMinerCLI(),
    WorkTimeMinerCLI(),
    PageRankCalculationCLI(),
    CNMatrixCalculationCLI()
).main(args)
