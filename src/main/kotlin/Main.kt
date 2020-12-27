import gitMiners.UtilGitMiner
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository


fun main() {
    val neededBranches  = setOf("control-3", "origin/control-3-dev")
    val repoJava = FileRepository("/home/nikolaisv/study/java/java-2_2020/.git/")
    val git = Git(repoJava)

    println(UtilGitMiner.findNeededBranchesOrNull(git, neededBranches))

}