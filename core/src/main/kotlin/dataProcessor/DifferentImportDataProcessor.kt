package dataProcessor

import dataProcessor.entity.LowerTriangularMatrixCounter
import dataProcessor.inputData.FilesChangeset

class DifferentImportDataProcessor(val module: String) : DataProcessorMapped<FilesChangeset>() {

    val counter = LowerTriangularMatrixCounter()

    override fun processData(data: FilesChangeset) {
        data.changeset.find { it.startsWith(module) } ?: return
        data.changeset.find { !it.startsWith(module) } ?: return

        val moduleFiles = mutableListOf<Int>()
        val nonModuleFiles = mutableListOf<Int>()

        for (filePath in data.changeset) {
            if (filePath.startsWith(module)) {
                moduleFiles.add(fileMapper.add(filePath))
            } else {
                nonModuleFiles.add(fileMapper.add(filePath))
            }
        }

        for (moduleFileId in moduleFiles) {
            for (nonModuleFileId in nonModuleFiles) {
                counter.increment(moduleFileId, nonModuleFileId)
            }
        }
    }

    override fun calculate() {}
}
