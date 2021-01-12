import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.junit.After
import org.junit.Before
import java.io.File

internal interface  GitMinerTest {
    companion object {
        val repositoryDir = File("src/test/repository")
        val resourcesOneThreadDir = File("src/test/resultsOneThread")
        val resourcesMultithreadingDir = File("src/test/resultsMultithreading")
    }

    @Before
    fun `load repository`() {
        deleteAll()
        repositoryDir.mkdirs()
        resourcesOneThreadDir.mkdirs()
        resourcesMultithreadingDir.mkdirs()

        Git.cloneRepository()
            .setURI("https://github.com/facebook/react.git")
            .setDirectory(repositoryDir)
            .setNoCheckout(true)
            .call().use { result ->
                println("Having repository: " + result.repository.directory)
            }
    }

    @After
    fun deleteAll() {
        deleteDir(resourcesOneThreadDir)
        deleteDir(resourcesMultithreadingDir)
        deleteDir(repositoryDir)
    }

    fun deleteDir(directory: File) {
        if (directory.exists() && directory.isDirectory) {
            try {
                FileUtils.deleteDirectory(directory)
            } catch (e: Exception) {
                println("Got error while cleaning directory $directory: $e")
            }
        }
    }
}
