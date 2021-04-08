package dataProcessor

import dataProcessor.ChangedFilesDataProcessor.UserChangedFiles
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet

class ChangedFilesDataProcessor : DataProcessorMapped<UserChangedFiles>() {

    private val _userFilesIds = ConcurrentHashMap<Int, ConcurrentSkipListSet<Int>>()

    val userFilesIds: Map<Int, Set<Int>>
        get() = _userFilesIds

    // TODO: Same class in FileDependencyMatrix
    data class UserChangedFiles(val user: String, val files: Set<String>)

    override fun processData(data: UserChangedFiles) {
        val userId = userMapper.add(data.user)
        for (filePath in data.files) {
            val fileId = fileMapper.add(filePath)
            _userFilesIds.computeIfAbsent(userId) { ConcurrentSkipListSet() }.add(fileId)
        }
    }

    override fun calculate() {}
}