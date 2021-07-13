package visualisation

import calculations.CoordinationNeedsMatrixCalculation
import dataProcessor.AssignmentMatrixDataProcessor
import dataProcessor.FileDependencyMatrixDataProcessor
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import miners.gitMiners.FileDependencyMatrixMiner
import miners.gitMiners.UserChangedFilesMiner
import util.HeapNStorage
import util.HelpFunctionsUtil
import visualisation.entity.Edge
import visualisation.entity.GraphD3
import visualisation.entity.Node
import java.io.File


class WeightedGraph {
    val graph: GraphD3
    private val edgeStorage: HeapNStorage<Edge>

    // proceed square matrix
    constructor(data: Array<out FloatArray>, size: Int, descending: Boolean = false, multigraph: Boolean = false) {
        // TODO: .reversed()?
        val comparator = if (descending) compareByDescending<Edge> { it.weight } else compareBy { it.weight }
        edgeStorage = HeapNStorage(size, comparator)
        if (!multigraph) {
            for (i in 0..data.size) {
                for (j in 0 until data.size - i) {
                    edgeStorage.add(Edge(i.toString(), j.toString(), data[i][j]))
                }
            }
        } else {

        }

        val nodes = mutableSetOf<Node>()
        for (edge in edgeStorage) {
            nodes.add(Node(edge.source, 1f))
            nodes.add(Node(edge.target, 1f))
        }

        graph = GraphD3(nodes.toList(), edgeStorage.toList())

//        val jsonString = Json.encodeToString(graph)
        println(data.size)
    }

    private fun save() {

    }

}

fun main() {
//    val arr = Array(3) { FloatArray(3) }
//    for (i in 0..2) {
//        for (j in 0..2) {
//            arr[i][j] = (i + j).toFloat()
//            print("${arr[i][j]} ")
//        }
//        println()
//    }
//    WeightedGraph(arr, 2)
//    val repositoryDirectory = File("/home/nikolaisv/study/intership/work/repos/react/.git")
//    val branches = setOf("master")
//    val assignmentMatrixDataProcessor = AssignmentMatrixDataProcessor()
//    val userChangedFilesMiner = UserChangedFilesMiner(repositoryDirectory, branches)
//    userChangedFilesMiner.run(assignmentMatrixDataProcessor)
//    val numOfUsers = assignmentMatrixDataProcessor.idToUser.size
//
//    val fileDependencyDataProcessor = FileDependencyMatrixDataProcessor()
//    val fileDependencyMiner = FileDependencyMatrixMiner(repositoryDirectory, branches)
//    fileDependencyMiner.run(fileDependencyDataProcessor)
//
//    val numOfFiles = fileDependencyDataProcessor.idToFile.size
//    val fileDependencyMatrix = HelpFunctionsUtil.convertMapToArray(
//        HelpFunctionsUtil.changeKeysInMapOfMaps(
//            fileDependencyDataProcessor.fileDependencyMatrix,
//            fileDependencyDataProcessor.idToFile, assignmentMatrixDataProcessor.fileToId,
//            fileDependencyDataProcessor.idToFile, assignmentMatrixDataProcessor.fileToId
//
//        ),
//        numOfFiles,
//        numOfFiles
//    )
//
//    val assignmentMatrix = HelpFunctionsUtil.convertMapToArray(
//        assignmentMatrixDataProcessor.assignmentMatrix,
//        numOfUsers,
//        numOfFiles
//    )
//
//    val calculation = CoordinationNeedsMatrixCalculation(
//        fileDependencyMatrix,
//        assignmentMatrix
//    )
//    calculation.run()
//
//    HelpFunctionsUtil.saveToJson(
//        File("./CN_data.json"),
//        calculation.coordinationNeeds
//    )

    val CN = Json.decodeFromString<Array<out FloatArray>>(File("./CN_data.json").readText())

    val wg = WeightedGraph(CN, 5000)
    val jsonString = Json.encodeToString(wg.graph)
    val file = File("./data.js")
    file.writeText("const data = " + jsonString)
}