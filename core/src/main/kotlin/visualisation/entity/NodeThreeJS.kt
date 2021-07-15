package visualisation.entity

import kotlinx.serialization.Serializable

@Serializable
data class NodeThreeJS(
    val id: String,
    val value: Int = 1,
    val color: String = "#323ca8",
    val shape: Int = 0
) : Comparable<NodeThreeJS> {
    override fun compareTo(other: NodeThreeJS): Int {
        return id.compareTo(other.id)
    }
}