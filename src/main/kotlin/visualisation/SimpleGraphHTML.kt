package visualisation

import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import kotlinx.html.unsafe
import java.io.File

class SimpleGraphHTML(
    private val nodes: Set<GraphHTML.NodeInfo>,
    private val edges: Set<GraphHTML.EdgeInfo>
) : GraphHTML {

    override fun draw(graphName: String, fileForSave: File) {
        val text = buildString {
            appendLine("<!DOCTYPE html>")
            appendHTML().html {
                unsafe {
                    raw(UtilVisualisation.defaultHead(graphName))
                }

                unsafe {
                    raw(UtilVisualisation.defaultBody(nodes, edges))
                }
            }
            appendLine()
        }

        fileForSave.writeText(text)
    }

}
