package miners.gitMiners.exceptions

import java.io.File

class NotGitRepositoryException: Exception {
    constructor() : super()
    constructor(directory: File) : super("${directory.absolutePath} is not git repository.")
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}