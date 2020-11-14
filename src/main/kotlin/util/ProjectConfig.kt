package util

import org.eclipse.jgit.internal.storage.file.FileRepository

object ProjectConfig {
    val uri = "https://github.com/facebook/react.git"
    val repoDir = "./local_repository/"

    val repository = FileRepository("${repoDir}/.git")
}