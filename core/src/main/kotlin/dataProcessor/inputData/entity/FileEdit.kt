package dataProcessor.inputData.entity

data class FileEdit(
    val addBlock: List<String>,
    val deleteBlock: List<String>,
    val preStartLineNum: Int,
    val postStartLineNum: Int,
    val oldPath: String,
    val newPath: String
)