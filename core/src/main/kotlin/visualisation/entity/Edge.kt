package visualisation.entity

import kotlinx.serialization.Serializable

@Serializable
data class Edge(val source: String, val target: String, val weight: Float)
