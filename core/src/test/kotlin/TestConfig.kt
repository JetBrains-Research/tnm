import java.io.File

object TestConfig {
    private val tmpFolder = File("src/test/tmp/")
    val repositoryDir = File(tmpFolder, "repository")
    val resultsDir = File(tmpFolder, "results")
    val gitDir = File(repositoryDir, ".git")
    const val branch = "origin/trunk"
    val branches = setOf(branch)
}
