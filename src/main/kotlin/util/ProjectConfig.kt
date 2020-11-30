package util

import org.eclipse.jgit.internal.storage.file.FileRepository

object ProjectConfig {
    private const val URI = "https://github.com/facebook/react.git"
    private const val REPO_DIR = "./local_repository/"
    private const val RESOURCES_PATH = "./resources"

    // UserMapper
    const val USER_ID_PATH = "$RESOURCES_PATH/userToId"
    const val ID_USER_PATH = "$RESOURCES_PATH/idToUser"

    // FileMapper
    const val FILE_ID_PATH = "$RESOURCES_PATH/fileToId"

    // CommitMapper
    const val COMMIT_ID_PATH = "$RESOURCES_PATH/commitToId"
    const val ID_COMMIT_PATH = "$RESOURCES_PATH/idToCommit"

    // PageRankMiner
    const val COMMITS_GRAPH_PATH = "$RESOURCES_PATH/commitsGraph"
    const val CONCURRENT_GRAPH_PATH = "$RESOURCES_PATH/concurrentGraph"

    // FilesOwnershipMiner
    const val FILES_OWNERSHIP_PATH = "$RESOURCES_PATH/filesOwnership"
    const val POTENTIAL_OWNERSHIP_PATH = "$RESOURCES_PATH/potentialAuthorship"
    const val DEVELOPER_KNOWLEDGE_PATH = "$RESOURCES_PATH/developerKnowledge"

    // FileDependencyMatrixMiner
    const val FILE_DEPENDENCY_PATH = "$RESOURCES_PATH/fileDependencyMatrix"

    // ChangedFilesMiner
    const val USER_FILES_IDS_PATH = "$RESOURCES_PATH/userFilesIds"

    // AssignmentMatrixMiner
    const val ASSIGNMENT_MATRIX_PATH = "$RESOURCES_PATH/assignmentMatrix"

    // CalcCNMatrix
    const val CN_MATRIX_PATH = "$RESOURCES_PATH/CN"
    val repository = FileRepository("${REPO_DIR}/.git")
}