package miners.gitMiners

import dataProcessor.WorkTimeDataProcessor
import dataProcessor.inputData.UserCommitDate
import org.eclipse.jgit.revwalk.RevCommit
import util.ProjectConfig
import java.io.File
import java.util.*


/**
 * Class for mining time distribution of commits for each user.
 * As the result creates map of time and number of commits. Each
 * key (time) represents minutes in week.
 *
 * @property repository
 * @constructor Create empty Work time miner
 */
class WorkTimeMiner(
    repositoryFile: File,
    neededBranches: Set<String>,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) : GitMiner<WorkTimeDataProcessor>(repositoryFile, neededBranches, numThreads = numThreads) {

    override fun process(dataProcessor: WorkTimeDataProcessor, commit: RevCommit) {
        val user = commit.authorIdent.emailAddress
        val date = Date(commit.commitTime * 1000L)

        val data = UserCommitDate(user, date)
        dataProcessor.processData(data)
    }
}
