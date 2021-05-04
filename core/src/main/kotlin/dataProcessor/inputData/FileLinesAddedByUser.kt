package dataProcessor.inputData

import java.util.*

data class FileLinesAddedByUser(val addedLines: IntRange, val filePath: String, val user: String, val date: Date) :
    InputData