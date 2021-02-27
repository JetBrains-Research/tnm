package gitMiners

import kotlinx.serialization.builtins.serializer
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevCommit
import util.Mapper
import util.ProjectConfig
import util.UserMapper
import util.UtilFunctions
import util.serialization.ConcurrentHashMapSerializer
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit


/**
 * Class for mining time distribution of commits for each user.
 * As the result creates map of time and number of commits. Each
 * key (time) represents minutes in week.
 *
 * @property repository
 * @constructor Create empty Work time miner
 */
class WorkTimeMiner(
    repository: FileRepository,
    neededBranches: Set<String> = ProjectConfig.DEFAULT_NEEDED_BRANCHES,
    numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) : GitMiner(repository, neededBranches, numThreads = numThreads) {

    // [user][minuteInWeek] = numOfCommits
    private val workTimeDistribution = ConcurrentHashMap<Int, ConcurrentHashMap<Int, Int>>()
    private val serializer = ConcurrentHashMapSerializer(
        Int.serializer(),
        ConcurrentHashMapSerializer(Int.serializer(), Int.serializer())
    )

    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        val email = currCommit.authorIdent.emailAddress
        val userId = UserMapper.add(email)

        val calendar: Calendar = GregorianCalendar.getInstance()
        val date = Date(currCommit.commitTime * 1000L)
        calendar.time = date

        val time = (TimeUnit.DAYS.toMinutes(calendar[Calendar.DAY_OF_WEEK].toLong()) +
                TimeUnit.HOURS.toMinutes(calendar[Calendar.HOUR_OF_DAY].toLong()) +
                calendar[Calendar.MINUTE]).toInt()

        workTimeDistribution
            .computeIfAbsent(userId) { ConcurrentHashMap() }
            .compute(time) { _, v -> if (v == null) 1 else v + 1 }
    }

    override fun saveToJson(resourceDirectory: File) {
        UtilFunctions.saveToJson(
            File(resourceDirectory, ProjectConfig.WORKTIME_DISTRIBUTION),
            workTimeDistribution, serializer
        )
        Mapper.saveAll(resourceDirectory)
    }
}
