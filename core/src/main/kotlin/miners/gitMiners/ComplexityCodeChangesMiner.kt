package miners.gitMiners

import dataProcessor.ComplexityCodeChangesDataProcessor
import dataProcessor.ComplexityCodeChangesDataProcessor.ChangeType
import dataProcessor.inputData.CommitFilesModifications
import dataProcessor.inputData.FileModification
import miners.gitMiners.GitMinerUtil.isBugFixCommit
import miners.gitMiners.GitMinerUtil.isNotNeededFilePath
import miners.gitMiners.exceptions.ProcessInThreadPoolException
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.util.io.DisabledOutputStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import util.ProjectConfig
import util.TrimmedDate
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future


class ComplexityCodeChangesMiner(
    repositoryFile: File,
    private val neededBranch: String,
    val filesToProceed: Set<String>? = null,
    numOfCommits: Int? = null,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) : GitMiner<ComplexityCodeChangesDataProcessor>(repositoryFile, setOf(neededBranch), numOfCommits, numThreads) {

    private val log: Logger = LoggerFactory.getLogger(ComplexityCodeChangesMiner::class.java)

    override fun process(
        dataProcessor: ComplexityCodeChangesDataProcessor,
        commit: RevCommit
    ) {
        val filesModifications = extractData(commit, dataProcessor.changeType)
        val data = CommitFilesModifications(TrimmedDate.getTrimDate(commit), filesModifications)
        dataProcessor.processData(data)
    }

    private fun extractData(commit: RevCommit, changeType: ChangeType): Iterable<FileModification> {
        if (!isFeatureIntroductionCommit(commit)) return emptyList()

        val reader = threadLocalReader.get()

        val result = mutableListOf<FileModification>()
        when (changeType) {
            ChangeType.LINES -> {
                val diffs = reader.use { GitMinerUtil.getDiffsWithoutText(commit, it, repository) }
                val diffFormatter = DiffFormatter(DisabledOutputStream.INSTANCE)
                diffFormatter.setRepository(repository)
                diffFormatter.setDiffComparator(RawTextComparator.DEFAULT)
                diffFormatter.isDetectRenames = true

                for (diff in diffs) {
                    val filePath = GitMinerUtil.getFilePath(diff)
                    if (isNotNeededFilePath(filePath, filesToProceed)) continue

                    val editList = diffFormatter.toFileHeader(diff).toEditList()
                    for (edit in editList) {
                        val numOfAddedLines = edit.endB - edit.beginB
                        val numOfDeletedLines = edit.endA - edit.beginA

                        val modifiedLines = numOfAddedLines + numOfDeletedLines
                        result.add(FileModification(filePath, modifiedLines))
                    }
                }
            }

            ChangeType.FILE -> {
                val changedFiles = reader.use {
                    GitMinerUtil.getChangedFiles(
                        commit, it, repository
                    )
                }

                for (filePath in changedFiles) {
                    if (isNotNeededFilePath(filePath, filesToProceed)) continue
                    result.add(FileModification(filePath, 1))
                }
            }
        }
        return result
    }

    override fun run(dataProcessor: ComplexityCodeChangesDataProcessor) {
        val git = threadLocalGit.get()
        val branch = GitMinerUtil.findNeededBranch(git, neededBranch)
        val commitsInBranch = getUnprocessedCommits(branch.name)
        if (commitsInBranch.isEmpty()) {
            log.info("Nothing to proceed in branch $branch")
            return
        }
        val lastCommit = commitsInBranch.first()
        dataProcessor.init(TrimmedDate.getTrimDate(lastCommit))
        super.run(dataProcessor)
    }

    private fun isFeatureIntroductionCommit(commit: RevCommit): Boolean {
        return !isBugFixCommit(commit)
    }

}
