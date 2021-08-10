package calculations

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import util.HelpFunctionsUtil


class CoordinationNeedsMatrixCalculation(
    private val fileDependencyMatrix: INDArray,
    private val assignmentMatrix: INDArray
) : Calculation {

    constructor(fileDependencyArray: Array<FloatArray>, assignmentArray: Array<FloatArray>) : this(
        Nd4j.create(fileDependencyArray),
        Nd4j.create(assignmentArray)
    )

    private var _coordinationNeeds: INDArray? = null
    val coordinationNeeds: Array<out FloatArray>
        get() = _coordinationNeeds?.toFloatMatrix() ?: emptyArray()

    override fun run() {
        _coordinationNeeds = assignmentMatrix.mmul(fileDependencyMatrix).mmul(assignmentMatrix.transpose())
        _coordinationNeeds?.let { HelpFunctionsUtil.normalizeMax(it) }
    }
}
