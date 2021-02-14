package cli.visualizationCLI

import cli.InfoCLI
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file

abstract class VisualizationCLI(infoCLI: InfoCLI) :
    CliktCommand(name = infoCLI.name, help = infoCLI.help) {
    protected val name by option("--name", help = "Name of graph")
        .required()

}