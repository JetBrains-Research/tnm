package cli.gitMinersCLI.base

abstract class GitMinerMultithreadedMultipleBranchesCLI(name: String, help: String) :
    GitMinerMultipleBranchesCLI(name, help) {
    protected val numThreads by numOfThreadsOption()
}