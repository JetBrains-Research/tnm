package cli

import cli.calculculationsCLI.CoordinationNeedsMatrixCalculationCLI
import cli.calculculationsCLI.PageRankCalculationCLI
import cli.gitMinersCLI.*
import cli.visualizationCLI.WeightedEdgesGraphCLI
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class CLI : CliktCommand() {
    override fun run() {}
}

// TODO: add CoEditNetworkMiner
fun main(args: Array<String>) = CLI().subcommands(
    AssignmentMatrixMinerCLI(),
    ChangedFilesMinerCLI(),
    CoEditNetworksMinerCLI(),
    FileDependencyMatrixMinerCLI(),
    FilesOwnershipMinerCLI(),
    CommitInfluenceGraphMinerCLI(),
    WorkTimeMinerCLI(),
    PageRankCalculationCLI(),
    CoordinationNeedsMatrixCalculationCLI(),
    WeightedEdgesGraphCLI()
).main(args)
