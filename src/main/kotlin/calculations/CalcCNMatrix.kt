package calculations

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler
import org.nd4j.linalg.factory.Nd4j
import util.ProjectConfig
import util.UtilFunctions
import java.io.File


class CalcCNMatrix(private val resourceDirectory: File) {

    fun run(thresholdForAssignmentMatrix: Int = 10) {
        val jsonFileMapper = File(resourceDirectory, ProjectConfig.FILE_ID).readText()
        val fileMap = Json.decodeFromString<HashMap<String, Int>>(jsonFileMapper)
        val numOfFiles = fileMap.size

        val jsonUserMapper = File(resourceDirectory, ProjectConfig.USER_ID).readText()
        val userMap = Json.decodeFromString<HashMap<String, Int>>(jsonUserMapper)
        val numOfUsers = userMap.size

        val fileD = File(resourceDirectory, ProjectConfig.FILE_DEPENDENCY)
        val D = UtilFunctions.loadArray(fileD, numOfFiles, numOfFiles)

        val fileA = File(resourceDirectory, ProjectConfig.ASSIGNMENT_MATRIX)
        val A = UtilFunctions.loadArray(fileA, numOfUsers, numOfFiles)
//        BooleanIndexing.replaceWhere(A,0.0, Conditions.lessThan(thresholdForAssignmentMatrix))
//        BooleanIndexing.replaceWhere(A,1.0, Conditions.greaterThanOrEqual(thresholdForAssignmentMatrix))

        val CN = A.mmul(D).mmul(A.transpose())

        UtilFunctions.normalizeMax(CN)
        UtilFunctions.saveToJson(File(resourceDirectory, ProjectConfig.CN_MATRIX), CN.toFloatMatrix())
    }



}


fun main() {
    val calc = CalcCNMatrix(File(ProjectConfig.RESOURCES_PATH))
    calc.run()
}