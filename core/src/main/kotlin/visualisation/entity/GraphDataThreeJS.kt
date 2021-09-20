package visualisation.entity

import kotlinx.serialization.Serializable

@Serializable
data class GraphDataThreeJS(val nodes: List<NodeThreeJS>, val links: List<EdgeThreeJS>)
