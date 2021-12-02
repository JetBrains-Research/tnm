package dataProcessor.initData

import dataProcessor.initData.entity.FileLineOwnedByUser
import kotlinx.serialization.Serializable
import util.serializers.DateAsLongSerializer
import java.util.*

@Serializable
data class LatestCommitOwnedLines(
    @Serializable(with = DateAsLongSerializer::class)
    val latestCommitDate: Date,
    val linesOwnedByUser: List<FileLineOwnedByUser>) :
    InitData
