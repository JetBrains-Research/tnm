package visualisation.entity

data class NodeInfo(val id: Int, val value: Float) : Comparable<NodeInfo> {
    override fun compareTo(other: NodeInfo): Int {
        return value.compareTo(other.value)
    }
}