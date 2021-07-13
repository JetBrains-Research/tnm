package visualisation.entity

import kotlinx.serialization.Serializable

@Serializable
data class Node(val id: String, val weight: Float) : Comparable<Node> {
    override fun compareTo(other: Node): Int {
        return id.compareTo(other.id)
    }
}