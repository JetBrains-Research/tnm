package calculations

object MirrorCongruenceCalculation {

    fun run(
        Gs: Map<Int, Set<Int>>,
        Gp: Map<Int, Set<Int>>,
        J: Map<Int, Set<Int>>,
    ): Float {

        var y = 0
        var k = 0

        val inverseJ = inverseJ(J)

        for ((i, adjS) in Gs) {
            for (j in adjS) {
                // arc (i, j)
                val Ji = inverseJ[i] ?: continue
                val Jj = inverseJ[j] ?: continue

                for (userId1 in Ji) {
                    for (userId2 in Jj) {
                        if (userId1 != userId2) y++

                        val isFamiliar = Gp[userId1]?.contains(userId2) == true ||
                            Gp[userId2]?.contains(userId1) == true
                        if (isFamiliar) k++
                    }
                }
            }
        }
        val congruence = k.toFloat() / y.toFloat()
        return congruence
    }

    private fun inverseJ(J: Map<Int, Set<Int>>): Map<Int, Set<Int>> {
        val result = HashMap<Int, MutableSet<Int>>()

        for ((user, artifacts) in J) {
            for (artifact in artifacts) {
                result.computeIfAbsent(artifact) { mutableSetOf() }.add(user)
            }
        }

        return result
    }
}
