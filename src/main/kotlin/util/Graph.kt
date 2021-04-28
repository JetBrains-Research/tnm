package util

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet

class Graph<T> {
    private val _adjacencyMap: ConcurrentHashMap<T, ConcurrentSkipListSet<T>> = ConcurrentHashMap()
    val adjacencyMap: Map<T, Set<T>>
        get() = _adjacencyMap

    fun addEdge(sourceVertex: T, destinationVertex: T) {
        _adjacencyMap
            .computeIfAbsent(sourceVertex) { ConcurrentSkipListSet() }
            .add(destinationVertex)
    }

    fun addNode(node: T) {
        _adjacencyMap
            .computeIfAbsent(node) { ConcurrentSkipListSet() }
    }

    override fun toString(): String = StringBuffer().apply {
        for (key in _adjacencyMap.keys) {
            append("$key -> ")
            append(_adjacencyMap[key]?.joinToString(", ", "[", "]\n"))
        }
    }.toString()
}