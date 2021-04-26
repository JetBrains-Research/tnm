package cli.calculculationsCLI

import calculations.PageRankCalculation
import calculations.PageRankCalculation.Companion.DEFAULT_ALPHA
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.float
import dataProcessor.CommitInfluenceGraphDataProcessor
import miners.gitMiners.CommitInfluenceGraphMiner
import miners.gitMiners.UtilGitMiner
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevCommit
import util.UtilFunctions
import java.io.File

class PageRankCalculationCLI : CalculationCLI(
    "PageRankCalculation",
    "Computes PageRank vector which contains importance rankings (in the bug-fixing and bug creation context) " +
            "for each commit. The computation results are saved to a $HELP_PAGE_RANK."
) {

    companion object {
        const val SHORTNAME_ALPHA = "-a"
        const val LONGNAME_ALPHA = "--alpha"

        const val HELP_ALPHA = "Scalar parameter in [0, 1]. By default $DEFAULT_ALPHA"

        const val LONGNAME_PAGE_RANK = "--page-rank"
        const val HELP_PAGE_RANK = " JSON file as a vector, where j-th entry corresponds to the importance of a commit"
    }

    private val alpha by option(SHORTNAME_ALPHA, LONGNAME_ALPHA, help = HELP_ALPHA)
        .float()
        .default(DEFAULT_ALPHA)

    private val pageRankJsonFile by saveFileOption(
        LONGNAME_PAGE_RANK,
        HELP_PAGE_RANK,
        File(resultDir, "PageRank")
    )

    private val branches by branchesOption()
    private val numThreads by numOfThreadsOption()
    private val idToCommitJsonFile by idToCommitOption()

    private fun countCommits(repository: FileRepository): Int {
        val commits = HashSet<RevCommit>()
        for (branch in branches) {
            commits.addAll(UtilGitMiner.getCommits(Git(repository), repository, branch, false))
        }
        return commits.size
    }

    override fun run() {
        val repository = FileRepository(repositoryDirectory)
        val numOfCommits = countCommits(repository)
        val dataProcessor = CommitInfluenceGraphDataProcessor()
        val miner = CommitInfluenceGraphMiner(repository, branches, numThreads = numThreads)
        miner.run(dataProcessor)

        val calculation = PageRankCalculation(dataProcessor.adjacencyMap, numOfCommits, alpha)
        calculation.run()

        UtilFunctions.saveToJson(
            pageRankJsonFile,
            calculation.pageRank
        )

        UtilFunctions.saveToJson(
            idToCommitJsonFile,
            dataProcessor.idToCommit
        )

    }
}