import gitMiners.FilesOwnershipMiner
import org.eclipse.jgit.internal.storage.file.FileRepository


fun main() {
//    val uri = "https://github.com/facebook/react.git"
    val dir = "./local_repository"

    val repository = FileRepository("${dir}/.git")
//    val parseChangedFiles = ParseChangedFiles(repository)
//    parseChangedFiles.run()

//    val parseFileDependencyMatrix = ParseFileDependencyMatrix(repository)
//    parseFileDependencyMatrix.run()

//    val parseAssignmentMatrix = ParseAssignmentMatrix(repository)
//    parseAssignmentMatrix.run()

//    val gson = Gson()
//    val readerArtifacts = JsonReader(FileReader("./resources/fileDependencyMatrix"))
//    val readerAssignment = JsonReader(FileReader("./resources/assignmentMatrix"))
//    val artifactsRelations: Array<Array<Int>> = gson.fromJson(readerArtifacts, Array<Array<Int>>::class.java)
//    val assignmentMatrix: Array<Array<Int>> = gson.fromJson(readerAssignment, Array<Array<Int>>::class.java)

//    CalcMirrorCongruence(artifactsRelations, assignmentMatrix).run()

    val parse = FilesOwnershipMiner(repository)
    parse.run()
}