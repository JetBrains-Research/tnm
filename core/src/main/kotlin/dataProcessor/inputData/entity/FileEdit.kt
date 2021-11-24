package dataProcessor.inputData.entity

import kotlinx.serialization.Serializable

@Serializable
data class FileEdit(
    val addBlock: List<String>,
    val deleteBlock: List<String>,
    val preStartLineNum: Int,
    val postStartLineNum: Int,
    val oldPath: String,
    val newPath: String
)