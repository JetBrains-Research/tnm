package cli.calculculationsCLI

import calculations.MirrorCongruenceCalculation
import cli.AbstractCLI
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import util.HelpFunctionsUtil
import java.io.File

class MirrorCongruenceCalculationCLI: AbstractCLI(
  "MirrorCongruenceCalculationCLI",
  "Calculation of the socio-technical congruence. Needs Gp - adjacency map Map<Int, Set<Int>> where `Gp[userId] = Set(userId1, userId2...)` is " +
      "set of user ids representing undirected connection between developers. Gs - adjacency map Map<Int, Set<Int>> where `Gs[fileId] = Set(fileId1, fileId2...)` " +
      "set of file ids representing direct connection between files. J - adjacency map Map<Int, Set<Int>> where `J[userId] = Set(fileId1, fileId2...)` " +
      "set of file ids representing direct connections from users to files"
) {

  private val GpFile by loadFileOption("--Gp-map", "Gp - adjacency map Map<Int, Set<Int>> where `Gp[userId] = Set(userId1, userId2...)`. Can be triangular.")
  private val GsFile by loadFileOption("--Gs-map", "Gs - adjacency map Map<Int, Set<Int>> where `Gs[fileId] = Set(fileId1, fileId2...)`")
  private val JFile by loadFileOption("--J-map", "J - adjacency map Map<Int, Set<Int>> where `J[userId] = Set(fileId1, fileId2...)`")


  private val resultFile by saveFileOption(
    "--result-file",
    "File where to store results.",
    File(resultDir, "Congruence")
  )

  override fun run() {

    val Gp = decodeAdjMap(GpFile)
    val Gs = decodeAdjMap(GsFile)
    val J = decodeAdjMap(JFile)

    val congruence = MirrorCongruenceCalculation.run(Gs, Gp, J)

    HelpFunctionsUtil.saveToJson(
      resultFile,
      congruence
    )
  }

  private fun decodeAdjMap(file: File) = Json.decodeFromString<Map<Int, Set<Int>>>(file.readText())
}
