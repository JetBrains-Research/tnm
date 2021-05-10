import TestConfig.gitDir
import TestConfig.repositoryDir
import org.eclipse.jgit.api.Git
import org.junit.Before
import util.UtilFunctions

interface GitTest {

    @Before
    fun `load repository`() {

        if (UtilFunctions.isGitRepository(gitDir)) return

        val repoURI = "https://github.com/cli/cli.git"
        println("Loading repository for tests $repoURI")
        UtilFunctions.deleteDir(repositoryDir)
        repositoryDir.mkdirs()

        Git.cloneRepository()
            .setURI(repoURI)
            .setDirectory(repositoryDir)
            .setNoCheckout(true)
            .call().use { result ->
                println("Finish loading repo $repoURI")
                println("Repository inside: " + result.repository.directory)
            }
    }
}