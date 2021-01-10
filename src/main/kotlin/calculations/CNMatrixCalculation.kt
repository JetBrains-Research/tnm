package calculations

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.nd4j.linalg.api.ndarray.INDArray
import util.ProjectConfig
import util.UtilFunctions
import java.io.File


class CNMatrixCalculation(resourceDirectory: File) : Calculation {
    val D: INDArray
    val A: INDArray
    var CN: INDArray? = null
        private set

    init {
        val jsonFileMapper = File(resourceDirectory, ProjectConfig.FILE_ID).readText()
        val fileMap = Json.decodeFromString<HashMap<String, Int>>(jsonFileMapper)
        val numOfFiles = fileMap.size

        val jsonUserMapper = File(resourceDirectory, ProjectConfig.USER_ID).readText()
        val userMap = Json.decodeFromString<HashMap<String, Int>>(jsonUserMapper)
        val numOfUsers = userMap.size

        val fileD = File(resourceDirectory, ProjectConfig.FILE_DEPENDENCY)
        D = UtilFunctions.loadArray(fileD, numOfFiles, numOfFiles)

        val fileA = File(resourceDirectory, ProjectConfig.ASSIGNMENT_MATRIX)
        A = UtilFunctions.loadArray(fileA, numOfUsers, numOfFiles)
//        BooleanIndexing.replaceWhere(A,0.0, Conditions.lessThan(thresholdForAssignmentMatrix))
//        BooleanIndexing.replaceWhere(A,1.0, Conditions.greaterThanOrEqual(thresholdForAssignmentMatrix))
    }

    override fun run() {
        CN = A.mmul(D).mmul(A.transpose())
        CN?.let { UtilFunctions.normalizeMax(it) }
    }

    override fun saveToJson(resourceDirectory: File) {
        CN?.let { UtilFunctions.saveToJson(File(resourceDirectory, ProjectConfig.CN_MATRIX), it.toFloatMatrix()) }
    }

}
