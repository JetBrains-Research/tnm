package util

object ProjectConfig {
    val DEFAULT_NUM_THREADS = Runtime.getRuntime().availableProcessors()

    const val DEFAULT_BRANCH = "origin/main"
    val DEFAULT_NEEDED_BRANCHES = setOf(DEFAULT_BRANCH)
}
