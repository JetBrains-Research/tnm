package cli.gitMinersCLI.base

abstract class GitMinerOneBranchCLI(name: String, help: String) :
    GitMinerCLI(name, help) {
    protected val branch by oneBranchOption()
}