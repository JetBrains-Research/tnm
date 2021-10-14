package calculations

import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.operations.div
import org.jetbrains.kotlinx.multik.ndarray.operations.max


object CoordinationNeedsMatrixCalculation {

    fun run(
        fileDependencyMatrix: FloatArray,
        assignmentMatrix: FloatArray,
        numOfUsers: Int,
        numOfFiles: Int
    ): Array<FloatArray> {
        val D: D2Array<Float> = mk.ndarray(fileDependencyMatrix, numOfFiles, numOfFiles)
        val A: D2Array<Float> = mk.ndarray(assignmentMatrix, numOfUsers, numOfFiles)
        val CN = mk.linalg.dot(mk.linalg.dot(A, D), A.transpose())
        val coordinationNeeds = CN / CN.max()!!

        // TODO: possible memory problems
        val result = Array(numOfUsers) { FloatArray(numOfUsers) { 0f } }
        var idx = 0
        for (value in coordinationNeeds) {
            val row = idx / numOfUsers
            val column = idx % numOfUsers
            result[row][column] = value
            idx++
        }
        return result
    }

}
