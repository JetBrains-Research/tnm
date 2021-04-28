package calculations

import org.nd4j.linalg.api.ndarray.INDArray
import util.UtilFunctions


class CoordinationNeedsMatrixCalculation(
    private val fileDependencyMatrix: INDArray,
    private val assignmentMatrix: INDArray
) : Calculation {
    private var _coordinationNeeds: INDArray? = null
    val coordinationNeeds: Array<out FloatArray>
        get() = _coordinationNeeds?.toFloatMatrix() ?: emptyArray()

    override fun run() {
        _coordinationNeeds = assignmentMatrix.mmul(fileDependencyMatrix).mmul(assignmentMatrix.transpose())
        _coordinationNeeds?.let { UtilFunctions.normalizeMax(it) }
    }
}
