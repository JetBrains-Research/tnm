package miners.gitMiners

import GitTest
import TestConfig.gitDir
import TestConfig.repositoryDir
import TestConfig.resultsDir
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.eclipse.jgit.api.Git
import org.junit.Assert
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import util.HelpFunctionsUtil
import util.ProjectConfig
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


abstract class GitMinerTest<T> : GitTest {
    val previousResultFile: File
        get() = File(resultsDir, this::class.simpleName!!)

    abstract val serializer: KSerializer<T>

    companion object {
        private fun loadRepository() {

            if (HelpFunctionsUtil.isGitRepository(gitDir)) return

            val repoURI = "https://github.com/cli/cli.git"
            println("Loading repository for tests $repoURI")
            HelpFunctionsUtil.deleteDir(repositoryDir)
            repositoryDir.mkdirs()

            Git.cloneRepository()
                .setURI(repoURI)
                .setDirectory(repositoryDir)
                .setNoCheckout(true)
                .call().use { result ->
                    println("Finish loading repo $repoURI")
                    println("Repository inside: " + result.repository.directory)
                }
        }

        private fun prepare() {
            resultsDir.mkdirs()
            loadRepository()
        }

        private fun forEachGitMinerTest(processTestObj: (GitMinerTest<*>) -> Unit) {
            prepare()

            val packageName = "miners.gitMiners"
            val url = GitMinerTest::class.java.getResource(packageName.replace('.', '/'))
            val directory = File(url.getFile())

            directory.walk()
                .filter { f -> f.isFile && !f.name.contains('$') && f.name.endsWith(".class") }
                .forEach {
                    val className = packageName +
                            it.canonicalPath.removePrefix(directory.canonicalPath)
                                .removeSuffix(".class")
                                .replace('/', '.')

                    val myClass = Class.forName(className)
                    if (myClass.constructors.isNotEmpty()) {
                        val constructor = myClass.getDeclaredConstructor()
                        val objectTest = constructor.newInstance() as GitMinerTest<*>
                        processTestObj(objectTest)
                    }
                }
        }

        fun generateTestData() = forEachGitMinerTest { it.generateTestData() }

    }

    @Before
    fun preparations() {
        resultsDir.mkdirs()
    }

    @Test
    fun `test one thread and multithreading`() {
        val resultOneThread = runMiner(1)
        val resultMultithreading = runMiner()
        compareResults(resultOneThread, resultMultithreading)
    }

    @Test
    fun compareWithPrevious() {
        assumeTrue(previousResultFile.exists())
        val result1 = runMiner()
        val result2 = Json.decodeFromString(serializer, previousResultFile.readText())
        compareResults(result1, result2)
    }

    fun <V> changeIdsToValuesInMapOfMaps(
        map: Map<Int, Map<Int, V>>,
        keys1: Map<Int, String>,
        keys2: Map<Int, String>
    ): Map<String, Map<String, V>> {
        val newMap = HashMap<String, HashMap<String, V>>()
        for (entry1 in map) {
            for (entry2 in entry1.value) {
                val key1 = keys1[entry1.key]
                val key2 = keys2[entry2.key]
                assertNotNull(key1, "can't find key1 ${entry1.key}")
                assertNotNull(key2, "can't find key2 ${entry2.key}")
                newMap
                    .computeIfAbsent(key1) { HashMap() }
                    .computeIfAbsent(key2) { entry2.value }
            }
        }

        return newMap
    }

    fun changeIdsToValuesInMapOfSets(
        map: Map<Int, Set<Int>>,
        keys: Map<Int, String>,
        values: Map<Int, String>
    ): Map<String, Set<String>> {
        val newMap = HashMap<String, Set<String>>()
        for (entry1 in map) {
            val key = keys[entry1.key]
            assertNotNull(key, "can't find key ${entry1.key}")

            val set = mutableSetOf<String>()

            for (valueId in entry1.value) {
                val value = values[valueId]
                assertNotNull(value, "can't find value $valueId")
                set.add(value)
            }

            newMap[key] = set
        }

        return newMap
    }

    fun <T> compareMapsOfMaps(
        map1: Map<String, Map<String, T>>,
        map2: Map<String, Map<String, T>>
    ) {

        for (entry1 in map1.entries) {
            for (entryOfEntry1 in entry1.value.entries) {
                val k1 = entry1.key
                val k2 = entryOfEntry1.key

                val v1 = map1[k1]?.get(k2)
                assertNotNull(v1, "got null in v1 : [$k1][$k2]")

                val v2 = map2[k1]?.get(k2)
                assertNotNull(v2, "got null in v2 : [$k1][$k2]")

                assertEquals(v1, v2, "Found non equal values in [$k1][$k2]: $v1 != $v2")
            }
        }
    }

    fun compareMapsOfMapsDouble(
        map1: Map<String, Map<String, Float>>,
        map2: Map<String, Map<String, Float>>
    ) {

        for (entry1 in map1.entries) {
            val k1 = entry1.key
            for (entryOfEntry1 in entry1.value.entries) {
                val k2 = entryOfEntry1.key

                val v1 = map1[k1]?.get(k2)
                assertNotNull(v1, "got null in v1 : [$k1][$k2]")

                val v2 = map2[k1]?.get(k2)
                assertNotNull(v2, "got null in v2 : [$k1][$k2]")

                Assert.assertEquals(v1, v2, 0.0001f)
            }
        }
    }

    fun <T> compareSets(set1: Set<T>, set2: Set<T>) {
        assertTrue(
            set1.size == set2.size &&
                    set1.containsAll(set2) &&
                    set2.containsAll(set1),
            "Not equal $set1 != $set2"
        )
    }

    fun compareMapOfSets(
        map1: Map<String, Set<String>>,
        map2: Map<String, Set<String>>
    ) {
        for (entry in map1) {
            val userName = entry.key
            val value1 = entry.value
            val value2 = map2[userName]
            assertNotNull(value2, "got null in v2 for user $userName")
            compareSets(value1, value2)
        }
    }

    abstract fun runMiner(numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS): T

    abstract fun compareResults(result1: T, result2: T)

    fun generateTestData() {
        val data = runMiner()
        previousResultFile.writeText(Json.encodeToString(serializer, data))
    }
}
