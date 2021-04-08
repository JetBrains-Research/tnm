package dataProcessor

import dataProcessor.AssignmentMatrixDataProcessor.UserChangedFiles
import java.util.concurrent.ConcurrentHashMap

class AssignmentMatrixDataProcessor : DataProcessorMapped<UserChangedFiles>() {

    private val _assignmentMatrix: ConcurrentHashMap<Int, ConcurrentHashMap<Int, Int>> = ConcurrentHashMap()

    val assignmentMatrix: Map<Int, Map<Int, Int>>
        get() = _assignmentMatrix

    data class UserChangedFiles(val user: String, val files: Set<String>)

    override fun processData(data: UserChangedFiles) {
        val userId = userMapper.add(data.user)
        for (filePath in data.files) {
            val fileId = fileMapper.add(filePath)
            _assignmentMatrix
                .computeIfAbsent(userId) { ConcurrentHashMap() }
                .compute(fileId) { _, v -> if (v == null) 1 else v + 1 }
        }
    }

    override fun calculate() {}
}