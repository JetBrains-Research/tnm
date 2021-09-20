package miners.gitMiners

import dataProcessor.FileDependencyMatrixDataProcessor
import dataProcessor.inputData.FilesChangeset
import org.eclipse.jgit.revwalk.RevCommit
import util.ProjectConfig
import java.io.File

/**
 * Class for mining  file dependency matrix
 * For example:
 * Change sets {A,B,C} and {A,B} the dependency matrix entries in D would be
 * D[A,B] = 2, D[A,C] = 1, and D[B,C] = 1
 *
 */
class FileDependencyMatrixMiner(
    repositoryFile: File,
    neededBranches: Set<String>,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) : GitMiner<FileDependencyMatrixDataProcessor>(repositoryFile, neededBranches, numThreads = numThreads) {
    override fun process(
        dataProcessor: FileDependencyMatrixDataProcessor,
        commit: RevCommit
    ) {
        val git = threadLocalGit.get()
        val reader = repository.newObjectReader()

        val changedFiles = reader.use {
            UtilGitMiner.getChangedFiles(commit, it, git)
        }
        dataProcessor.processData(FilesChangeset(changedFiles))
    }
}
