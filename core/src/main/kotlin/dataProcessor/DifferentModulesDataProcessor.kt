package dataProcessor

import dataProcessor.entity.LowerTriangularMatrixCounter
import dataProcessor.inputData.FilesChangeset
import util.mappers.Mapper

class DifferentModulesDataProcessor(val modules: Set<String>) : DataProcessorMapped<FilesChangeset>() {
    class ModuleMapper : Mapper() {
        val moduleToId: Map<String, Int>
            get() = entityToId

        val idToModule: Map<Int, String>
            get() = idToEntity
    }

    val counter = LowerTriangularMatrixCounter()
    val moduleMapper = ModuleMapper()
    val fileToModuleId = HashMap<Int, Int>()

    init {
        modules.forEach { moduleMapper.add(it) }
    }

    private fun getIdNonCache(filePath: String): Int {
        for (modulePath in modules) {
            if (filePath.startsWith(modulePath)) return moduleMapper.moduleToId[modulePath]!!

        }
        return -1
    }

    private fun getId(filePath: String, fileId: Int): Int {
        fileToModuleId[fileId]?.let { return it }

        for (modulePath in modules) {
            if (filePath.startsWith(modulePath)) {
                val id = moduleMapper.moduleToId[modulePath]!!
                fileToModuleId[fileId] = id
                return id
            }
        }
        fileToModuleId[fileId] = -1
        return -1
    }

    private fun split(files: Set<String>): List<Set<Int>> {
        val result = ArrayList<MutableSet<Int>>()
        (0..modules.size).forEach { _ -> result.add(mutableSetOf()) }

        for (filePath in files) {
            val fileId = fileMapper.add(filePath)
            val id = getId(filePath, fileId)
            val arrayId = id + 1
            result[arrayId].add(fileId)
        }

        return result
    }

    private fun containsNotSameModuleFiles(files: Set<String>): Boolean {
        if (files.isEmpty()) return false

        val id = getIdNonCache(files.first())
        for (filePath in files) {
            if (id != getIdNonCache(filePath)) return true
        }
        return false
    }


    override fun processData(data: FilesChangeset) {
        if (!containsNotSameModuleFiles(data.changeset)) return

        val moduleFiles = split(data.changeset)

        for ((i, fileSet1) in moduleFiles.withIndex()) {
            for (fileSet2 in moduleFiles.subList(i + 1, moduleFiles.size)) {

                for (file1 in fileSet1) {
                    for (file2 in fileSet2) {
                        counter.increment(file1, file2)
                    }
                }

            }
        }
    }

    override fun calculate() {}
}
