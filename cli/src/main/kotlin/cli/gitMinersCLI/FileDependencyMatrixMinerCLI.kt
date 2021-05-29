package cli.gitMinersCLI

import cli.gitMinersCLI.base.GitMinerMultithreadedMultipleBranchesCLI
import dataProcessor.FileDependencyMatrixDataProcessor
import miners.gitMiners.FileDependencyMatrixMiner
import org.eclipse.jgit.internal.storage.file.FileRepository
import util.HelpFunctionsUtil
import java.io.File

class FileDependencyMatrixMinerCLI :
    GitMinerMultithreadedMultipleBranchesCLI(
        "FileDependencyMatrixMiner",
        "Miner yields a $HELP_FILE_DEPENDENCY_MATRIX"
    ) {

    companion object {
        const val HELP_FILE_DEPENDENCY_MATRIX = "JSON file with map of maps, where both inner and outer " +
                "keys are file ids and the value is the number of times both file has been edited in the same commit."
        const val LONGNAME_FILE_DEPENDENCY_MATRIX = "--file-dependency-matrix"
    }

    private val fileDependencyMatrixJsonFile by saveFileOption(
        LONGNAME_FILE_DEPENDENCY_MATRIX,
        HELP_FILE_DEPENDENCY_MATRIX,
        File(resultDir, "FileDependencyMatrix")
    )

    private val idToFileJsonFile by idToFileOption()

    override fun run() {
        val dataProcessor = FileDependencyMatrixDataProcessor()
        val miner = FileDependencyMatrixMiner(repositoryDirectory, branches, numThreads = numThreads)
        miner.run(dataProcessor)

        HelpFunctionsUtil.saveToJson(
            fileDependencyMatrixJsonFile,
            dataProcessor.fileDependencyMatrix
        )

        HelpFunctionsUtil.saveToJson(
            idToFileJsonFile,
            dataProcessor.idToFile
        )

    }
}