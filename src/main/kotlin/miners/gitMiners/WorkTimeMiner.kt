package miners.gitMiners

import dataProcessor.WorkTimeDataProcessor
import dataProcessor.WorkTimeDataProcessor.UserCommitDate
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevCommit
import util.ProjectConfig
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
    repository: FileRepository,
    neededBranches: Set<String>,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) : GitMiner<WorkTimeDataProcessor>(repository, neededBranches, numThreads = numThreads) {

    override fun process(dataProcessor: WorkTimeDataProcessor, currCommit: RevCommit, prevCommit: RevCommit) {
        val user = currCommit.authorIdent.emailAddress
        val date = Date(currCommit.commitTime * 1000L)

        val data = UserCommitDate(user, date)
        dataProcessor.processData(data)
    }
}
