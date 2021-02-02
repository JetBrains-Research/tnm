package util

import org.eclipse.jgit.internal.storage.file.FileRepository

object ProjectConfig {
    private const val URI = "https://github.com/facebook/react.git"
    private const val REPO_DIR = "./local_repository/"
    const val RESOURCES_PATH = "./resources"
    const val numThreads = 4

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

    // CO_EDIT
    const val CO_EDIT = "co_edit"

    val REPOSITORY = FileRepository("${REPO_DIR}/.git")
    val neededBranches = setOf("origin/master")
}