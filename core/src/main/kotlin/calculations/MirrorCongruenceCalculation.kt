package calculations

import util.Graph

object MirrorCongruenceCalculation {

    fun run(
        artifactsRelations: Array<Array<Int>>,
        assignmentMatrix: Array<Array<Int>>,
        developersRelations: Array<Array<Int>>
    ): Float {
        val Gs = createGraph(artifactsRelations)
        val Gp = createGraph(developersRelations)
        val J = createJ(
            artifactsRelations,
            assignmentMatrix
        )

        var y = 0
        var k = 0

        val numOfArtifacts = artifactsRelations.size
        for (i in 0 until numOfArtifacts) {
            for (j in 0 until numOfArtifacts) {
                if (Gs.adjacencyMap[i]?.contains(j) == true) {
                    for (userId1 in J.adjacencyMap[i]!!) {
                        for (userId2 in J.adjacencyMap[j]!!) {
                            if (userId1 != userId2) y++
                            if (Gp.adjacencyMap[userId1]?.contains(userId2) == true) k++
                        }
                    }
                }
            }
        }

        val congruence = k.toFloat() / y.toFloat()
        return congruence
    }

    private fun createGraph(adjacencyMatrix: Array<Array<Int>>): Graph<Int> {
        val result = Graph<Int>()
        val numOfRows = adjacencyMatrix.size
        val numOfColumns = adjacencyMatrix[0].size
        for (i in 0 until numOfRows) {
            for (j in 0 until numOfColumns) {
                if (i == j) continue
                if (adjacencyMatrix[i][j] != 0) {
                    result.addEdge(i, j)
                    result.addEdge(j, i)
                }
            }
        }
        return result
    }

    private fun createJ(
        artifactsRelations: Array<Array<Int>>,
        assignmentMatrix: Array<Array<Int>>,
    ): Graph<Int> {
        val result = Graph<Int>()
        val numOfArtifacts = artifactsRelations.size
        val numOfUsers = assignmentMatrix.size
        for (i in 0 until numOfUsers) {
            for (j in 0 until numOfArtifacts) {
                if (assignmentMatrix[i][j] != 0) result.addEdge(j, i)
            }
        }
        return result
    }

}
