import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import java.io.File

internal interface  GitMinerTest {
    companion object {
        val repositoryDir = File("src/test/repository")
        val resourcesOneThreadDir = File("src/test/resultsOneThread")
        val resourcesMultithreadingDir = File("src/test/resultsMultithreading")
    }

    @Before
    fun `load repository`() {
        val repoURI = "https://github.com/facebook/react.git"
        println("Loading repository for tests $repoURI")
        deleteAll()
        repositoryDir.mkdirs()
        resourcesOneThreadDir.mkdirs()
        resourcesMultithreadingDir.mkdirs()

        Git.cloneRepository()
            .setURI(repoURI)
            .setDirectory(repositoryDir)
            .setNoCheckout(true)
            .call().use { result ->
                println("Finish loading repo $repoURI")
                println("Repository inside: " + result.repository.directory)
            }
    }

        @After
    fun deleteAll() {
        println("Start cleaning results and loaded repository")
        deleteDir(resourcesOneThreadDir)
        deleteDir(resourcesMultithreadingDir)
        deleteDir(repositoryDir)
        println("End cleaning results and loaded repository")
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
