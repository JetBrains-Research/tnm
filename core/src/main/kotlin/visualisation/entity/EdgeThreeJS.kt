package visualisation.entity

import kotlinx.serialization.Serializable

@Serializable
data class EdgeThreeJS(
    val source: String,
    val target: String,
    val weight: Float,
    val value: Float = 0f,
    val color: String = "white"
)