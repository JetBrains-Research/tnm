package miners.gitMiners

import dataProcessor.DataProcessor
import dataProcessor.inputData.FilesChangeset
import org.eclipse.jgit.revwalk.RevCommit
import util.ProjectConfig
import java.io.File

class FilesChangesetMiner(
    repositoryFile: File,
    neededBranches: Set<String>,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS,
    numOfCommits: Int? = null,
    val maxNumOfFiles: Int? = null,
    val includeFiles: Set<String>? = null

) : GitMiner<DataProcessor<FilesChangeset>>(
    repositoryFile,
    neededBranches,
    numThreads = numThreads,
    numOfCommits = numOfCommits
) {
    override fun process(dataProcessor: DataProcessor<FilesChangeset>, commit: RevCommit) {
        val reader = repository.newObjectReader()

        val changedFiles = reader.use {
            GitMinerUtil.getChangedFiles(commit, it, repository)
        }

        maxNumOfFiles?.let { if (changedFiles.size > it) return }

        includeFiles?.let {
            it.forEach { filePath -> if (filePath in includeFiles) return@let }
            return
        }

        dataProcessor.processData(FilesChangeset(changedFiles))
    }
}
