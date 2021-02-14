package visualisation


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
