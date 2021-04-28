package cli.gitMinersCLI

import cli.gitMinersCLI.base.GitMinerMultithreadedMultipleBranchesCLI
import dataProcessor.CommitInfluenceGraphDataProcessor
import miners.gitMiners.CommitInfluenceGraphMiner
import org.eclipse.jgit.internal.storage.file.FileRepository
import util.UtilFunctions
import java.io.File

class CommitInfluenceGraphMinerCLI : GitMinerMultithreadedMultipleBranchesCLI(
    "CommitInfluenceGraphMiner",
    "Miner yields the $HELP_COMMITS_GRAPH"
) {
    companion object {
        const val HELP_COMMITS_GRAPH = "JSON file  with a map of lists, with key corresponding to the fixing " +
                "commit id and value corresponding to the commits with lines changed by fixes."
        const val LONGNAME_COMMITS_GRAPH = "--commits-graph"
    }

    private val commitsGraphJsonFile by saveFileOption(
        LONGNAME_COMMITS_GRAPH,
        HELP_COMMITS_GRAPH,
        File(resultDir, "CommitInfluenceGraph")
    )

    private val idToCommitJsonFile by idToCommitOption()

    override fun run() {
        val repository = FileRepository(repositoryDirectory)
        val dataProcessor = CommitInfluenceGraphDataProcessor()
        val miner = CommitInfluenceGraphMiner(repository, branches, numThreads = numThreads)
        miner.run(dataProcessor)

        UtilFunctions.saveToJson(
            commitsGraphJsonFile,
            dataProcessor.adjacencyMap
        )

        UtilFunctions.saveToJson(
            idToCommitJsonFile,
            dataProcessor.idToCommit
        )
    }
}
