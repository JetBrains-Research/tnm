package visualisation

import java.io.File

interface GraphHTML {

    fun draw(graphName: String, fileForSave: File)

    data class NodeInfo(
        val id: String, val label: String = id,
        val title: String = id, val shape: String = "dot", val color: String = "white"
    ) {

        fun generateVis(): String {
            return """
                |{
                |"font": {"color": "$color"}, 
                |"id": "$id", 
                |"label": "$label", 
                |"shape": "$shape", 
                |"title": "$title"
                |}
                |""".trimMargin()
        }

    }

    data class EdgeInfo(val from: String, val to: String, val color: String = "#FCAA05", val value: String = "0.06") {

        fun generateVis(): String {
            return """
                |{
                |"color": "$color", 
                |"from": "$from", 
                |"to": "$to", 
                |"value": $value
                |}
                |""".trimMargin()
        }

    }
}
