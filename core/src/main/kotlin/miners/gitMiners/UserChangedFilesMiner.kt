package miners.gitMiners

import dataProcessor.DataProcessor
import dataProcessor.inputData.UserChangedFiles
import org.eclipse.jgit.revwalk.RevCommit
import util.ProjectConfig
import java.io.File

class UserChangedFilesMiner(
    repositoryFile: File,
    neededBranches: Set<String>,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) : GitMiner<DataProcessor<UserChangedFiles>>(repositoryFile, neededBranches, numThreads = numThreads) {

    override fun process(dataProcessor: DataProcessor<UserChangedFiles>, commit: RevCommit) {
        val reader = threadLocalReader.get()

        val user = commit.authorIdent.emailAddress

        val changedFiles =
            reader.use {
                GitMinerUtil.getChangedFiles(commit, it, repository)
            }

        val data = UserChangedFiles(user, changedFiles)
        dataProcessor.processData(data)

    }

}
