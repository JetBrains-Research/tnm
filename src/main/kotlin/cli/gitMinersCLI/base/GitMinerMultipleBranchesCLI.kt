package cli.gitMinersCLI.base

abstract class GitMinerMultipleBranchesCLI(name: String, help: String) :
    GitMinerCLI(name, help) {
    protected val branches by branchesOption()
}
