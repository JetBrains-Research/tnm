package gitMiners

import dataProcessor.ChangedFilesDataProcessor
import dataProcessor.ChangedFilesDataProcessor.UserChangedFiles
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevCommit
import util.ProjectConfig

class ChangedFilesMiner(
    repository: FileRepository,
    neededBranches: Set<String> = ProjectConfig.DEFAULT_NEEDED_BRANCHES,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) : GitMiner<ChangedFilesDataProcessor>(repository, neededBranches, numThreads = numThreads) {

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
