package miners.gitMiners

import dataProcessor.ChangedFilesDataProcessor
import dataProcessor.inputData.UserChangedFiles
import org.eclipse.jgit.revwalk.RevCommit
import util.ProjectConfig
import java.io.File

class ChangedFilesMiner(
    repositoryFile: File,
    neededBranches: Set<String>,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) : GitMiner<ChangedFilesDataProcessor>(repositoryFile, neededBranches, numThreads = numThreads) {

    override fun process(dataProcessor: ChangedFilesDataProcessor, currCommit: RevCommit, prevCommit: RevCommit) {
        val git = threadLocalGit.get()
        val reader = threadLocalReader.get()

        val user = currCommit.authorIdent.emailAddress

        val changedFiles =
            reader.use {
                UtilGitMiner.getChangedFiles(currCommit, prevCommit, it, git)
            }

        val data = UserChangedFiles(user, changedFiles)
        dataProcessor.processData(data)

    }

}
