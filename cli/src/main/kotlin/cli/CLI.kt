package cli

import calculations.MirrorCongruenceCalculation
import cli.calculculationsCLI.CoordinationNeedsMatrixCalculationCLI
import cli.calculculationsCLI.MirrorCongruenceCalculationCLI
import cli.calculculationsCLI.PageRankCalculationCLI
import cli.gitMinersCLI.*
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class CLI : CliktCommand() {
    override fun run() {}
}

fun main(args: Array<String>) = CLI().subcommands(
    AssignmentMatrixMinerCLI(),
    ChangedFilesMinerCLI(),
    CoEditNetworksMinerCLI(),
    ComplexityCodeChangesCLI(),
    FileDependencyMatrixMinerCLI(),
    FilesOwnershipMinerCLI(),
    CommitInfluenceGraphMinerCLI(),
    WorkTimeMinerCLI(),
    PageRankCalculationCLI(),
    CoordinationNeedsMatrixCalculationCLI(),
    MirrorCongruenceCalculationCLI()
).main(args)
