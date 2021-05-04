package dataProcessor

import dataProcessor.inputData.UserChangedFiles
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet

class ChangedFilesDataProcessor : DataProcessorMapped<UserChangedFiles>() {

    private val _userFilesIds = ConcurrentHashMap<Int, ConcurrentSkipListSet<Int>>()

    val changedFilesByUsers: Map<Int, Set<Int>>
        get() = _userFilesIds

    override fun processData(data: UserChangedFiles) {
        val userId = userMapper.add(data.user)
        for (filePath in data.files) {
            val fileId = fileMapper.add(filePath)
            _userFilesIds.computeIfAbsent(userId) { ConcurrentSkipListSet() }.add(fileId)
        }
    }

    override fun calculate() {}
}