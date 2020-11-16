package util

class Graph<T> {
    val adjacencyMap: HashMap<T, HashSet<T>> = HashMap()

    fun addEdge(sourceVertex: T, destinationVertex: T) {
        adjacencyMap
                .computeIfAbsent(sourceVertex) { HashSet() }
                .add(destinationVertex)
    }

    fun addNode(node: T) {
        adjacencyMap
                .computeIfAbsent(node) { HashSet() }
    }

    override fun toString(): String = StringBuffer().apply {
        for (key in adjacencyMap.keys) {
            append("$key -> ")
            append(adjacencyMap[key]?.joinToString(", ", "[", "]\n"))
        }
    }.toString()
}