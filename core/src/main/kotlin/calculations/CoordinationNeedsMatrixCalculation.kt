package calculations

import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.operations.div
import org.jetbrains.kotlinx.multik.ndarray.operations.max


class CoordinationNeedsMatrixCalculation(
    fileDependencyMatrix: FloatArray,
    assignmentMatrix: FloatArray,
    numOfUsers: Int,
    numOfFiles: Int
) : Calculation {

    private val arrFDM: D2Array<Float>
    private val arrAM: D2Array<Float>

    init {
        arrFDM = mk.ndarray(fileDependencyMatrix, numOfFiles, numOfFiles)
        arrAM = mk.ndarray(assignmentMatrix, numOfUsers, numOfFiles)
    }

    private var _coordinationNeeds: D2Array<Float>? = null
    val coordinationNeeds: D2Array<Float>?
        get() = _coordinationNeeds

    override fun run() {
        val result = mk.linalg.dot(mk.linalg.dot(arrAM, arrFDM), arrAM.transpose())
        _coordinationNeeds = result / result.max()!!
    }
}
