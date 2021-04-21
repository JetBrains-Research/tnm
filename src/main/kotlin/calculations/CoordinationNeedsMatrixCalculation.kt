package calculations

import org.nd4j.linalg.api.ndarray.INDArray
import util.UtilFunctions


class CoordinationNeedsMatrixCalculation(
    private val fileDependencyMatrix: INDArray,
    private val assignmentMatrix: INDArray
) : Calculation {
    var coordinationsNeeds: INDArray? = null
        private set

    override fun run() {
        coordinationsNeeds = assignmentMatrix.mmul(fileDependencyMatrix).mmul(assignmentMatrix.transpose())
        coordinationsNeeds?.let { UtilFunctions.normalizeMax(it) }
    }

}
