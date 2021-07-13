package visualisation.entity

import kotlinx.serialization.Serializable

@Serializable
data class GraphD3(val nodes: List<Node>, val links: List<Edge>)
