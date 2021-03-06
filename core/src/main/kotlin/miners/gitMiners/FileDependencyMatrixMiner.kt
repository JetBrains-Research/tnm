package miners.gitMiners

import dataProcessor.FileDependencyMatrixDataProcessor
import dataProcessor.inputData.FilesChangeset
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevCommit
import util.ProjectConfig

/**
 * Class for mining  file dependency matrix
 * For example:
 * Change sets {A,B,C} and {A,B} the dependency matrix entries in D would be
 * D[A,B] = 2, D[A,C] = 1, and D[B,C] = 1
 *
 */
class FileDependencyMatrixMiner(
    repository: FileRepository,
    neededBranches: Set<String>,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) : GitMiner<FileDependencyMatrixDataProcessor>(repository, neededBranches, numThreads = numThreads) {
    override fun process(
        dataProcessor: FileDependencyMatrixDataProcessor,
        currCommit: RevCommit,
        prevCommit: RevCommit
    ) {
        val git = threadLocalGit.get()
        val reader = repository.newObjectReader()

        val changedFiles = reader.use {
            UtilGitMiner.getChangedFiles(currCommit, prevCommit, it, git)
        }
        dataProcessor.processData(FilesChangeset(changedFiles))
    }
}
