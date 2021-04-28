package miners.gitMiners.exceptions

class BranchNotExistsException : Exception {
    companion object {
        private fun errMsgNotFoundBranches(neededBranches: List<String>, allBranches: List<String>): String {
            val stringBuilder = StringBuilder()

            stringBuilder.appendLine()

            stringBuilder.appendLine("Couldn't find branches:")
            neededBranches.forEach { stringBuilder.appendLine(it) }

            stringBuilder.appendLine("Known branches:")
            allBranches.forEach { stringBuilder.appendLine(it) }

            return stringBuilder.toString()
        }

    }

    constructor() : super()
    constructor(neededBranches: List<String>, allBranches: List<String>) : super(
        errMsgNotFoundBranches(
            neededBranches,
            allBranches
        )
    )

    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
