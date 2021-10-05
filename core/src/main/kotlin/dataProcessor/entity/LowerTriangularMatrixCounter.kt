package dataProcessor.entity

import kotlin.math.sqrt

class LowerTriangularMatrixCounter(val chunkSize: Int = 10_000) {

    val chunks = ArrayList<Array<Int>>()

    private fun getChunkId(index: Int) = index / chunkSize

    private fun getRowIndex(index: Int) = index % chunkSize

    private fun getIndex(row: Int, column: Int): Int {
        val (i, j) =
            if (row > column) {
                row to column
            } else {
                column to row
            }
        return Math.addExact(Math.multiplyExact(i, i - 1) / 2, j)
    }

    private fun extendChunks(index: Int) {
        val neededSize = getChunkId(index) + 1
        val size = chunks.size
        for (i in size until neededSize) {
            chunks.add(Array(chunkSize) { 0 })
        }
    }

    fun increment(row: Int, column: Int) {
        val index = getIndex(row, column)

        extendChunks(index)

        val chunk = chunks[getChunkId(index)]
        chunk[getRowIndex(index)] += 1
    }

    operator fun get(row: Int, column: Int): Int {
        val index = getIndex(row, column)
        val chunk = chunks[getChunkId(index)]
        return chunk[getRowIndex(index)]
    }

    fun toMap(): HashMap<Int, HashMap<Int, Int>> {
        val result = HashMap<Int, HashMap<Int, Int>>()

        for ((chunkId, chunk) in chunks.withIndex()) {
            for ((valueId, value) in chunk.withIndex()) {

                if (value == 0) continue

                val index = chunkId * chunkSize + valueId

                val row = ((1.0 + sqrt((1.0 + 8 * index))) / 2).toInt()
                val column = index - Math.multiplyExact(row, row - 1) / 2

                result
                    .computeIfAbsent(row) { HashMap() }[column] = value
            }
        }

        return result
    }
}
