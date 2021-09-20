package cli.gitMinersCLI

import cli.gitMinersCLI.base.GitMinerMultithreadedOneBranchCLI
import dataProcessor.CoEditNetworksDataProcessor
import miners.gitMiners.CoEditNetworksMiner
import org.eclipse.jgit.internal.storage.file.FileRepository
import util.HelpFunctionsUtil
import java.io.File

class CoEditNetworksMinerCLI : GitMinerMultithreadedOneBranchCLI(
    "CoEditNetworksMiner",
    "Miner yields $HELP_CO_EDIT_NETWORKS"
) {

    companion object {
        const val HELP_CO_EDIT_NETWORKS = "JSON file with dict of commits information and list of edits. " +
                "Each edit includes pre/post file path, start line, length, number of chars, " +
                "entropy of changed block of code, Levenshtein distance between previous and new block of code, type of edit."
        const val LONGNAME_CO_EDIT_NETWORKS = "--co-edit-networks"
    }

    private val coEditNetworksJsonFile by saveFileOption(
        LONGNAME_CO_EDIT_NETWORKS,
        HELP_CO_EDIT_NETWORKS,
        File(resultDir, "CoEdits")
    )

    private val idToUserJsonFile by idToUserOption()
    private val idToFileJsonFile by idToFileOption()
    private val idToCommitJsonFile by idToCommitOption()

    override fun run() {
        val dataProcessor = CoEditNetworksDataProcessor()
        val miner = CoEditNetworksMiner(repositoryDirectory, branch, numThreads = numThreads)
        miner.run(dataProcessor)

        HelpFunctionsUtil.saveToJson(
            coEditNetworksJsonFile,
            dataProcessor.coEdits
        )

        HelpFunctionsUtil.saveToJson(
            idToUserJsonFile,
            dataProcessor.idToUser
        )

        HelpFunctionsUtil.saveToJson(
            idToFileJsonFile,
            dataProcessor.idToFile
        )

        HelpFunctionsUtil.saveToJson(
            idToCommitJsonFile,
            dataProcessor.idToCommit
        )
    }
}