package dataProcessor

import dataProcessor.entity.LowerTriangularMatrixCounter
import dataProcessor.inputData.FilesChangeset

/**
 * Class for file dependency matrix
 * For example:
 * Change sets {A,B,C} and {A,B} the dependency matrix entries in D would be
 * D[A,B] = 2, D[A,C] = 1, and D[B,C] = 1
 *
 */
class FileDependencyMatrixDataProcessor : DataProcessorMapped<FilesChangeset>() {

    val counter = LowerTriangularMatrixCounter()

    override fun processData(data: FilesChangeset) {
        val dataList = data.changeset.map { fileMapper.add(it) }

        for ((index, currFileId) in dataList.withIndex()) {
            for (otherFileId in dataList.subList(index + 1, dataList.lastIndex + 1)) {
                counter.increment(currFileId, otherFileId)
            }
        }
    }

    override fun calculate() {}

}
