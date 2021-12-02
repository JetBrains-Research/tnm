package dataProcessor.initData.entity

import kotlinx.serialization.Serializable

@Serializable
data class FileLineOwnedByUser(val lineNumber: Int, val filePath: String, val user: String)
