import org.eclipse.jgit.internal.storage.file.FileRepository
import java.io.File

object TestConfig {
    private val tmpFolder = File("src/test/tmp/")
    val repositoryDir = File(tmpFolder, "repository")
    val gitDir = File(repositoryDir, ".git")
    val repository = FileRepository(gitDir)
    const val branch = "origin/trunk"
    val branches = setOf(branch)
}
