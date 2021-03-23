import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.junit.After
import org.junit.Assert
import org.junit.Before
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal interface GitMinerTest {
    companion object {
        val repositoryDir = File("src/test/repository")
        val resourcesOneThreadDir = File("src/test/resultsOneThread")
        val resourcesMultithreadingDir = File("src/test/resultsMultithreading")
    }

    @Before
    fun `load repository`() {
        deleteAll()
        val repoURI = "https://github.com/facebook/react.git"
        println("Loading repository for tests $repoURI")
        repositoryDir.mkdirs()
        resourcesOneThreadDir.mkdirs()
        resourcesMultithreadingDir.mkdirs()

        Git.cloneRepository()
            .setURI(repoURI)
            .setDirectory(repositoryDir)
            .setNoCheckout(true)
            .call().use { result ->
                println("Finish loading repo $repoURI")
                println("Repository inside: " + result.repository.directory)
            }
    }

    @After
    fun deleteAll() {
        println("Start cleaning results and loaded repository")
        deleteDir(resourcesOneThreadDir)
        deleteDir(resourcesMultithreadingDir)
        deleteDir(repositoryDir)
        println("End cleaning results and loaded repository")
    }

    fun deleteDir(directory: File) {
        if (directory.exists() && directory.isDirectory) {
            try {
                FileUtils.deleteDirectory(directory)
            } catch (e: Exception) {
                println("Got error while cleaning directory $directory: $e")
            }
        }
    }

    fun <T> changeIdsToValuesInMapOfMaps(
        map: HashMap<Int, HashMap<Int, T>>,
        keys1: HashMap<Int, String>,
        keys2: HashMap<Int, String>
    ): HashMap<String, HashMap<String, T>> {
        val newMap = HashMap<String, HashMap<String, T>>()
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
        map: HashMap<Int, Set<Int>>,
        keys: HashMap<Int, String>,
        values: HashMap<Int, String>
    ): HashMap<String, Set<String>> {
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
        mapOneThread: HashMap<String, HashMap<String, T>>,
        mapMultithreading: HashMap<String, HashMap<String, T>>
    ) {

        for (entry1 in mapOneThread.entries) {
            for (entry2 in entry1.value.entries) {
                val k1 = entry1.key
                val k2 = entry2.key

                val v1 = mapOneThread[k1]?.get(k2)
                assertNotNull(v1, "got null in v1 : [$k1][$k2]")

                val v2 = mapMultithreading[k1]?.get(k2)
                assertNotNull(v2, "got null in v2 : [$k1][$k2]")

                assertEquals(v1, v2, "Found non equal values in [$k1][$k2]: $v1 != $v2")
            }
        }
    }

    fun compareMapsOfMapsDouble(
        mapOneThread: HashMap<String, HashMap<String, Double>>,
        mapMultithreading: HashMap<String, HashMap<String, Double>>
    ) {

        for (entry1 in mapOneThread.entries) {
            for (entry2 in entry1.value.entries) {
                val k1 = entry1.key
                val k2 = entry2.key

                val v1 = mapOneThread[k1]?.get(k2)
                assertNotNull(v1, "got null in v1 : [$k1][$k2]")

                val v2 = mapMultithreading[k1]?.get(k2)
                assertNotNull(v2, "got null in v2 : [$k1][$k2]")

                Assert.assertEquals(v1, v2, 0.0001)
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
        mapOneThread: HashMap<String, Set<String>>,
        mapMultithreading: HashMap<String, Set<String>>
    ) {
        for (entry in mapOneThread) {
            val userName = entry.key
            val valuesOneThread = entry.value
            val valuesMultithreading = mapMultithreading[userName]
            assertNotNull(valuesMultithreading, "got null in v2 for user $userName")
            compareSets(valuesOneThread, valuesMultithreading)
        }
    }
}
