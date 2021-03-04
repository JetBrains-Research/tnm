package util

object ProjectConfig {
    val DEFAULT_NUM_THREADS = Runtime.getRuntime().availableProcessors()

    // UserMapper
    const val USER_ID = "userToId"
    const val ID_USER = "idToUser"

    // FileMapper
    const val FILE_ID = "fileToId"
    const val ID_FILE = "idToFile"

    // CommitMapper
    const val COMMIT_ID = "commitToId"
    const val ID_COMMIT = "idToCommit"

    // PageRankMiner
    const val COMMITS_GRAPH = "CommitInfluenceGraph"

    // FilesOwnershipMiner
    const val FILES_OWNERSHIP = "FilesOwnership"
    const val POTENTIAL_OWNERSHIP = "PotentialAuthorship"
    const val DEVELOPER_KNOWLEDGE = "DeveloperKnowledge"

    // FileDependencyMatrixMiner
    const val FILE_DEPENDENCY = "FileDependencyMatrix"

    // ChangedFilesMiner
    const val USER_FILES_IDS = "userFilesIds"

    // AssignmentMatrixMiner
    const val ASSIGNMENT_MATRIX = "AssignmentMatrix"

    // WorkTimeMiner
    const val WORKTIME_DISTRIBUTION = "WorkTime"

    // CoordinationNeedsMatrix
    const val CN_MATRIX = "CoordinationNeeds"

    // PageRank result
    const val PAGERANK_MATRIX = "PageRank"

    // MirrorCongruence
    const val MIRROR_CONGRUENCE = "MirrorCongruence"

    // CoEditNetworks
    const val CO_EDIT = "CoEdit"

    const val DEFAULT_BRANCH = "origin/master"
    val DEFAULT_NEEDED_BRANCHES = setOf(DEFAULT_BRANCH)
}
