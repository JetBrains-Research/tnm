package visualisation.chart

import dataProcessor.ComplexityCodeChangesDataProcessor
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import miners.gitMiners.ComplexityCodeChangesMiner
import java.io.File
import java.text.SimpleDateFormat
import kotlin.random.Random

class ComplexityCodeChangesChart(
    val dataProcessor: ComplexityCodeChangesDataProcessor,
    val miner: ComplexityCodeChangesMiner,
    val repoPath: String,
    val analysedPath: String,
    val size: Int = 36
) {

    private val filesKey = "$analysedPath/..."
    private val pathFiles = "${repoPath}${analysedPath}"
    private val periodToLabel = mutableMapOf<Int, String>()

    @Serializable
    data class Dataset(
        val data: List<Double>,
        val fill: Boolean = false,
        val label: String = "Data",
        val backgroundColor: String = "rgb(255, 99, 132)",
        val borderColor: String = "rgba(255, 99, 132, 0.4)",
        val yAxisID: String = "y1",
        val type: String = "line",
    )

    @Serializable
    data class ExportData(val labels: List<String>, val datasets: List<Dataset>)

    @Serializable
    data class EntropyFileTree(val name: String, var entropy: Double, val children: MutableMap<String, EntropyFileTree>)

    private fun createTree(
        periodsToStats: Map<Int, ComplexityCodeChangesDataProcessor.PeriodStats>,
        idToFile: Map<Int, String>
    ): Map<Int, EntropyFileTree> {
        val periodToTree = mutableMapOf<Int, EntropyFileTree>()
        for (entry in periodsToStats) {
            val period = entry.value
            val periodId = entry.key
            val root = EntropyFileTree("", 0.0, HashMap())

            for (entry2 in period.filesStats) {
                val fileName = idToFile[entry2.key]!!
                val entropy = entry2.value.entropy
                root.entropy += entropy
                var node = root
                for (part in fileName.split('/')) {
                    node = node.children.computeIfAbsent(part) { EntropyFileTree(part, 0.0, HashMap()) }
                    node.entropy += entropy
                }
            }

            periodToTree[periodId] = root
        }
        return periodToTree
    }

    private fun getTopMeanDatasets(exportValues: Map<String, List<Double>>, n: Int = 15): List<Dataset> {
        val values =
            exportValues.entries.sortedByDescending { it.value.reduce { acc, d -> acc + d } / it.value.size }.take(n)
        return values.map {
            val r = Random.nextInt(0, 255)
            val g = Random.nextInt(0, 255)
            val b = Random.nextInt(0, 255)
            Dataset(
                it.value.reversed().takeLast(size),
                fill = false,
                label = it.key,
                backgroundColor = "rgb($r, $g, $b)",
                borderColor = "rgb($r, $g, $b, 0.4)"
            )
        }
    }

    private fun createExportData(): ExportData {
        miner.run(dataProcessor)

        val dateFormat = SimpleDateFormat("MM/yyyy")
        for (entry in miner.periodToDate) {
            periodToLabel[entry.key] = dateFormat.format(entry.value)
        }

        val entropyTree = createTree(dataProcessor.periodsToStats, dataProcessor.idToFile)

        val dirs = File(pathFiles).listFiles()
            .filter { it.isDirectory }
            .map { it.path.replace(repoPath, "") }

        val exportValues = HashMap<String, MutableList<Double>>()

        // Get entropy of dirs in [pathFiles]
        val keys = entropyTree.keys.sorted()
        for (key in keys) {
            for (dir in dirs) {
                var node = entropyTree[key]!!
                var flag = true
                val parts = dir.split("/")
                for (part in parts) {
                    val nextNode = node.children[part]
                    if (nextNode == null) {
                        exportValues.computeIfAbsent(parts.last()) { mutableListOf() }.add(0.0)
                        flag = false
                        break
                    } else {
                        node = nextNode
                    }
                }
                if (flag) {
                    exportValues.computeIfAbsent(parts.last()) { mutableListOf() }.add(node.entropy)
                }
            }
            exportValues.computeIfAbsent(analysedPath) { mutableListOf() }.add(entropyTree[key]!!.entropy)
        }

        // Get entropy of files in [pathFiles]
        for (key in keys) {
            var node = entropyTree[key]!!
            var flag = true
            for (part in analysedPath.split("/")) {
                val nextNode = node.children[part]
                if (nextNode == null) {
                    flag = false
                    break
                }
                node = nextNode
            }
            if (flag) {
                exportValues.computeIfAbsent(filesKey) { mutableListOf() }
                    .add(node.children.map { it.value.entropy }.reduce { acc, d -> acc + d })
            } else {
                exportValues.computeIfAbsent(filesKey) { mutableListOf() }.add(0.0)
            }
        }

        val datasets = getTopMeanDatasets(exportValues).toMutableList()
        val commitsData =
            dataProcessor.commitsInPeriod.keys
                .sortedDescending()
                .map { dataProcessor.commitsInPeriod[it]!!.toDouble() }
                .takeLast(size)

        datasets.add(
            Dataset(
                commitsData,
                label = "Commits",
                backgroundColor = "rgb(0, 0, 255, 0.4)",
                borderColor = "rgb(0, 0, 255, 0.4)",
                yAxisID = "y2",
                type = "bar",
            )
        )

        val labels =
            periodToLabel.keys.sortedDescending().map { periodToLabel[it]!! }
                .takeLast(size)

        return ExportData(
            labels,
            datasets
        )

    }

    fun saveChartData(directory: File) {
        val exportData = createExportData()
        val json = Json { encodeDefaults = true }
        val code = "const data = ${json.encodeToString(exportData)} \n export default data"
        File(directory, "data.js").writeText(code)
    }
}
