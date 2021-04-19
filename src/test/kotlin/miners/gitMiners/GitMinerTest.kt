package miners.gitMiners

import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.LsRemoteCommand
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.RepositoryCache
import org.eclipse.jgit.util.FS
import org.junit.After
import org.junit.Assert
import org.junit.Before
import util.UtilFunctions
import java.io.File
import java.util.*
import kotlin.collections.HashMap
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


internal interface GitMinerTest {
    companion object {
        val repositoryDir = File("src/test/tmp/repository")
        val gitDir = File(repositoryDir, ".git")
        val repository = FileRepository(gitDir)
        const val branch = "origin/trunk"
        val branches = setOf(branch)
    }

    @Before
    fun `load repository`() {

        if (UtilFunctions.isGitRepository(gitDir)) return

        val repoURI = "https://github.com/cli/cli.git"
        println("Loading repository for tests $repoURI")
        deleteDir(repositoryDir)
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
        map: Map<Int, Map<Int, T>>,
        keys1: Map<Int, String>,
        keys2: Map<Int, String>
    ): Map<String, Map<String, T>> {
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
        mapOneThread: Map<String, Map<String, T>>,
        mapMultithreading: Map<String, Map<String, T>>
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
        mapOneThread: Map<String, Map<String, Double>>,
        mapMultithreading: Map<String, Map<String, Double>>
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
        mapOneThread: Map<String, Set<String>>,
        mapMultithreading: Map<String, Set<String>>
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