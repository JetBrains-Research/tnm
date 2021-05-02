package dataProcessor.inputData.entity

import org.eclipse.jgit.revwalk.RevCommit

data class CommitInfo(
    val hash: String,
    val author: String,
    val date: Long
) {
    constructor(commit: RevCommit)
            : this(
        commit.name,
        commit.authorIdent.emailAddress,
        commit.commitTime * 1000L
    )

    constructor() : this("", "", 0)
}