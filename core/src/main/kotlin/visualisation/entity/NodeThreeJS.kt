package visualisation.entity

import kotlinx.serialization.Serializable

@Serializable
data class NodeThreeJS(
    val id: String,
    val value: Float = 1f,
    val color: String = "#323ca8",
    val shape: Int = 0
)