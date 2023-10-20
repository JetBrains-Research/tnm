package dataProcessor.inputData

import util.TrimmedDate

data class CommitFilesModifications(val trimmedDate: TrimmedDate, val filesModifications: Iterable<FileModification>) :
    InputData
