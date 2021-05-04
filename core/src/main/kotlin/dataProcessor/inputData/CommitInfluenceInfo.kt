package dataProcessor.inputData

data class CommitInfluenceInfo(val bugFixCommit: String, val prevCommit: String, val adjCommits: Set<String>) :
    InputData