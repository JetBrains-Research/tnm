package cli

import GitTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Before
import util.HelpFunctionsUtil.deleteDir
import java.io.File
import kotlin.test.assertTrue

abstract class AbstractCLITest(val testFolder: File) : GitTest {

    companion object {
        val tmpCLITestFolder = File("src/test/tmp/testCLI")
    }

    val idToUserJsonFile = File(testFolder, "idToUser")
    val idToFileJsonFile = File(testFolder, "idToFile")
    val idToCommitJsonFile = File(testFolder, "idToCommit")

    protected fun createInput(
        requiredOptions: List<Pair<String, String>>,
        nonRequiredOptions: List<Pair<String, String>>,
        arguments: List<String>
    ): List<String> {
        val result = mutableListOf<String>()

        for (option in requiredOptions) {
            result.add(option.first)
            result.add(option.second)
        }

        for (option in nonRequiredOptions) {
            result.add(option.first)
            result.add(option.second)
        }

        for (arg in arguments) {
            result.add(arg)
        }

        return result
    }

    protected fun checkIdToEntity(file: File) {
        val idToEntity = Json.decodeFromString<Map<Int, String>>(file.readText())
        assertTrue(idToEntity.isNotEmpty())
    }

    @Before
    fun `clean results`() {
        deleteDir(testFolder)
    }

}