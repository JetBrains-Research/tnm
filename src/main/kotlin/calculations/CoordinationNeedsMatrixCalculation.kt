package calculations

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.nd4j.linalg.api.ndarray.INDArray
import util.ProjectConfig
import util.UtilFunctions
import java.io.File


class CoordinationNeedsMatrixCalculation(resourceDirA: File, resourceDirD: File) : Calculation {
    val D: INDArray
    val A: INDArray
    var CN: INDArray? = null
        private set

    init {
        val jsonFileMapperA = File(resourceDirA, ProjectConfig.FILE_ID).readText()
        val fileToIdA = Json.decodeFromString<HashMap<String, Int>>(jsonFileMapperA)

        val jsonFileMapperD = File(resourceDirD, ProjectConfig.ID_FILE).readText()
        val idToFileD = Json.decodeFromString<HashMap<Int, String>>(jsonFileMapperD)

        val jsonUserMapperA = File(resourceDirA, ProjectConfig.USER_ID).readText()
        val userMapA = Json.decodeFromString<HashMap<String, Int>>(jsonUserMapperA)

        val numOfFiles = fileToIdA.size
        val numOfUsers = userMapA.size

        val fileA = File(resourceDirA, ProjectConfig.ASSIGNMENT_MATRIX)
        A = UtilFunctions.loadArray(fileA, numOfUsers, numOfFiles)

        val fileD = File(resourceDirD, ProjectConfig.FILE_DEPENDENCY)
        D = UtilFunctions.loadArray(fileD, numOfFiles, numOfFiles, idToFileD, fileToIdA)
    }

    override fun run() {
        CN = A.mmul(D).mmul(A.transpose())
        CN?.let { UtilFunctions.normalizeMax(it) }
    }

    override fun saveToJson(resourceDirectory: File) {
        CN?.let { UtilFunctions.saveToJson(File(resourceDirectory, ProjectConfig.CN_MATRIX), it.toFloatMatrix()) }
    }

}
