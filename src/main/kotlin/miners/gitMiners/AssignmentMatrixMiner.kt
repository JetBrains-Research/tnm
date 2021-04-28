package miners.gitMiners

import dataProcessor.AssignmentMatrixDataProcessor
import dataProcessor.AssignmentMatrixDataProcessor.UserChangedFiles
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevCommit
import util.ProjectConfig


/**
 * Assignment matrix miner
 *
 * @property repository
 * @constructor Create empty Assignment matrix miner for [repository] and store the results
 */
class AssignmentMatrixMiner(
    repository: FileRepository,
    neededBranches: Set<String>,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) : GitMiner<AssignmentMatrixDataProcessor>(repository, neededBranches, numThreads = numThreads) {

    override fun process(dataProcessor: AssignmentMatrixDataProcessor, currCommit: RevCommit, prevCommit: RevCommit) {
        val git = threadLocalGit.get()
        val reader = threadLocalReader.get()

        val changedFiles = reader.use {
            UtilGitMiner.getChangedFiles(currCommit, prevCommit, it, git)
        }

        val user = currCommit.authorIdent.emailAddress
        val data = UserChangedFiles(user, changedFiles)
        dataProcessor.processData(data)

    }

}
