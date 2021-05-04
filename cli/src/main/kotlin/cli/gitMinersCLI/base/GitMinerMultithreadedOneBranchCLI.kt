package cli.gitMinersCLI.base

abstract class GitMinerMultithreadedOneBranchCLI(name: String, help: String) : GitMinerOneBranchCLI(name, help) {
    protected val numThreads by numOfThreadsOption()
}