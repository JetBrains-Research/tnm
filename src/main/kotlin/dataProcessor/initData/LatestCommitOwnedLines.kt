package dataProcessor.initData

import dataProcessor.initData.entity.FileLineOwnedByUser
import java.util.*

data class LatestCommitOwnedLines(val latestCommitDate: Date, val linesOwnedByUser: List<FileLineOwnedByUser>) :
    InitData
