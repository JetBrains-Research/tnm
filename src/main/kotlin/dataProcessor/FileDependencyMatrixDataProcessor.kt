package dataProcessor

import util.FileMapper
import java.util.concurrent.ConcurrentHashMap

class FileDependencyMatrixDataProcessor : DataProcessor<List<String>> {
    val fileMapper = FileMapper()

    private val _fileDependencyMatrix: ConcurrentHashMap<Int, ConcurrentHashMap<Int, Int>> =
        ConcurrentHashMap()

    val fileDependencyMatrix: Map<Int, Map<Int, Int>>
        get() = _fileDependencyMatrix

    override fun processData(data: List<String>) {

        for ((index, currFile) in data.withIndex()) {
            for (otherFile in data.subList(index, data.lastIndex)) {
                val currFileId = fileMapper.add(currFile)
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