package calculations

import util.Graph

/**
 * Calc mirror congruence
 *
 * @property artifactsRelations
 * @property assignmentMatrix
 * @constructor Create empty Calc mirror congruence
 */
class CalcMirrorCongruence(
    private val artifactsRelations: Array<Array<Int>>,
    private val assignmentMatrix: Array<Array<Int>>
) {

    fun run() {
        val Gs = createGraph(artifactsRelations)
        val Gp = createGraph(calcPeopleRelations(3))
        val J = createJ()

        var y = 0
        var k = 0

        val numOfArtifacts = artifactsRelations.size
        for (i in 0 until numOfArtifacts) {
            for (j in 0 until numOfArtifacts) {
                if (Gs.adjacencyMap[i]?.contains(j) == true) {
                    // TODO: replace !!, ?:
                    for (userId1 in J.adjacencyMap[i]!!) {
                        for (userId2 in J.adjacencyMap[j]!!) {
                            if (userId1 != userId2) y++
                            if (Gp.adjacencyMap[userId1]?.contains(userId2) == true) k++
                        }
                    }
                }
            }
        }
        println("k = ${k}")
        println("y = ${y}")
        println(k.toFloat() / y.toFloat())
    }

    fun _run() {
        val peopleRelations = calcPeopleRelations()
        var y = 0
        var k = 0

        val numOfArtifacts = artifactsRelations.size
        val numOfUsers = assignmentMatrix.size
        for (i in 0 until numOfArtifacts) {
            for (j in 0 until numOfArtifacts) {
                if (artifactsRelations[i][j] == 0) continue

                for (userId1 in 0 until numOfUsers) {
                    for (userId2 in 0 until numOfUsers) {
                        if (userId1 != userId2) y++
                        if (peopleRelations[userId1][userId2] != 0) k++
                    }
                }
            }
        }


        println(k / y)

    }

    private fun calcPeopleRelations(threshold: Int = 10): Array<Array<Int>> {
        val numOfUsers = assignmentMatrix.size
        val numOfFiles = assignmentMatrix[0].size

        val result = Array(numOfUsers) { Array(numOfFiles) { 0 } }

        for (j in 0 until numOfFiles) {
            val users = arrayListOf<Int>()
            for (i in 0 until numOfUsers) {
                if (assignmentMatrix[i][j] >= threshold) {
                    users.add(i)
                }
            }

            for ((index, userId1) in users.withIndex()) {
                for (userId2 in users.subList(index, users.lastIndex)) {
                    result[userId1][userId2] = 1
                    result[userId2][userId1] = 1
                }
            }
        }

        return result
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

    fun createJ(): Graph<Int> {
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