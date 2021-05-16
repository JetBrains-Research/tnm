package calculations

import util.Graph

/**
 * Calc mirror congruence
 *
 * @property artifactsRelations
 * @property assignmentMatrix
 * @constructor Create empty Calc mirror congruence
 */
class MirrorCongruenceCalculation(
    private val artifactsRelations: Array<Array<Int>>,
    private val assignmentMatrix: Array<Array<Int>>,
    private val developersRelations: Array<Array<Int>>
) : Calculation {
    var congruence: Float? = null
        private set

    override fun run() {
        val Gs = createGraph(artifactsRelations)
        val Gp = createGraph(developersRelations)
        val J = createJ()

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
        congruence = k.toFloat() / y.toFloat()
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

    private fun createJ(): Graph<Int> {
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
