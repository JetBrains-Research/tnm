package dataProcessor.inputData

import dataProcessor.inputData.entity.CommitInfo
import dataProcessor.inputData.entity.FileEdit

data class CoEditInfo(
    val prevCommitInfo: CommitInfo,
    val commitInfo: CommitInfo,
    val nextCommitInfo: CommitInfo,
    val edits: List<FileEdit>
) : InputData