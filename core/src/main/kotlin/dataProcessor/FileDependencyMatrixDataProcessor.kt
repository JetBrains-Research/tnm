package dataProcessor

import dataProcessor.inputData.FilesChangeset
import java.util.concurrent.ConcurrentHashMap

class FileDependencyMatrixDataProcessor : DataProcessorMapped<FilesChangeset>() {

    private val _fileDependencyMatrix: ConcurrentHashMap<Int, ConcurrentHashMap<Int, Int>> =
        ConcurrentHashMap()

    val fileDependencyMatrix: Map<Int, Map<Int, Int>>
        get() = _fileDependencyMatrix

    override fun processData(data: FilesChangeset) {
        val dataList = data.changeset.toList()

        for ((index, currFile) in dataList.withIndex()) {
            val currFileId = fileMapper.add(currFile)
            for (otherFile in dataList.subList(index, dataList.lastIndex)) {
                val otherFileId = fileMapper.add(otherFile)

                if (currFileId == otherFileId)
                    continue


                increment(currFileId, otherFileId)
                increment(otherFileId, currFileId)
            }
        }
    }

    override fun calculate() {}

    private fun increment(fileId1: Int, fileId2: Int) {
        _fileDependencyMatrix
            .computeIfAbsent(fileId1) { ConcurrentHashMap() }
            .compute(fileId2) { _, v -> if (v == null) 1 else v + 1 }
    }
}