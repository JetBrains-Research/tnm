package visualisation.graph

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import kotlinx.serialization.encodeToString
import util.HelpFunctionsUtil
import visualisation.entity.GraphDataThreeJS
import java.io.File

abstract class GraphThreeJS(private val graphJsFileName: String) {
    companion object {
        private const val DATA_JS_FILENAME = "data.js"
        private const val GRAPH_HTML_ID = "3d-graph"
        private const val HTML_FILENAME = "graph.html"
        private const val THREE_JS_LIB = "https://unpkg.com/three@0.130.1/build/three.js"
        private const val GRAPH_JS_LIB = "https://unpkg.com/3d-force-graph@1.70.5/dist/3d-force-graph.min.js"

        protected fun getResourceAsText(path: String): String {
            return object {}.javaClass.getResource(path).readText()
        }




        fun edgeColor(
            value: Float,
            quantile1: String = "#0569E1",
            quantile2: String = "#C1F823",
            quantile3: String = "#FCAA05",
            quantile4: String = "#EE5503"
        ): String {
            return when {
                (value <= 0.25f) -> quantile1
                (value <= 0.5f) -> quantile2
                (value <= 0.75f) -> quantile3
                else -> quantile4
            }
        }

        fun normalizeMinMax(value: Float, min: Float, max: Float): Float {
            return (value - min) / (max - min)
        }

    }

    private fun createGraphJS(directory: File) {
        val script = getResourceAsText("/$graphJsFileName")
        File(directory, graphJsFileName).writeText(script)
    }

    private fun createHTML(directory: File) {
        val html = buildString {
            appendLine("<!DOCTYPE html>")
            appendHTML().html {
                body {
                    div {
                        id = GRAPH_HTML_ID
                    }
                    script {
                        src = THREE_JS_LIB
                    }
                    script {
                        src = GRAPH_JS_LIB
                    }
                    script {
                        src = File(directory, DATA_JS_FILENAME).absolutePath
                    }
                    script {
                        src = File(directory, graphJsFileName).absolutePath
                    }
                }
            }
        }

        File(directory, HTML_FILENAME).writeText(html)
    }

    abstract fun generateData(size: Int, descending: Boolean): GraphDataThreeJS

    fun saveGraph(directory: File, size: Int, descending: Boolean) {

        when {
            !directory.exists() -> {
                directory.mkdirs()
            }

            directory.exists() && !directory.isDirectory -> {
                throw Exception("Wrong path to save graph. There are already exists file: ${directory.absolutePath}")
            }
        }

        createHTML(directory)

        val graph = generateData(size, descending)
        createDataJS(directory, graph)

        createGraphJS(directory)

    }

    private fun createDataJS(directory: File, graph: GraphDataThreeJS) {
        val dataJson = HelpFunctionsUtil.json.encodeToString(graph)
        File(directory, DATA_JS_FILENAME).writeText("const data = $dataJson")
    }
}
