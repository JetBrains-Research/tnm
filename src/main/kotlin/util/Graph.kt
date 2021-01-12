package util

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet

class Graph<T> {
    val adjacencyMap: ConcurrentHashMap<T, ConcurrentSkipListSet<T>> = ConcurrentHashMap()

    fun addEdge(sourceVertex: T, destinationVertex: T) {
        adjacencyMap
            .computeIfAbsent(sourceVertex) { ConcurrentSkipListSet() }
            .add(destinationVertex)
    }

    fun addNode(node: T) {
        adjacencyMap
            .computeIfAbsent(node) { ConcurrentSkipListSet() }
    }

    override fun toString(): String = StringBuffer().apply {
        for (key in adjacencyMap.keys) {
            append("$key -> ")
            append(adjacencyMap[key]?.joinToString(", ", "[", "]\n"))
        }
    }.toString()
}