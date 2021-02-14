package visualisation

data class NodeInfo(
    val id: String, val label: String = id,
    val title: String = id, val shape: String = "dot", val textColor: String = "white",
    val background: String = "blue",
    val border: String = "black",
    val size: String = "1"
) {

    fun generateVis(): String {
        return """
                |{
                |"font": {"color": "$textColor"}, 
                |"id": "$id", 
                |"label": "$label", 
                |"shape": "$shape",
                | "size": $size,
                |"title": "$title",
                |"color": {"background": "$background", "border": "$border"}
                |}
                |""".trimMargin()
    }

}