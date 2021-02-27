package cli.calculculationsCLI

import cli.InfoCLI
import cli.UtilCLI
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file

abstract class CalculationCLI(infoCLI: InfoCLI) :
    CliktCommand(name = infoCLI.name, help = infoCLI.help) {
    protected val resources by option(UtilCLI.LONGNAME_RESOURCES, help = UtilCLI.HELP_RESOURCES_OPT)
        .file(mustExist = true, canBeDir = true, canBeFile = false)
        .required()
}