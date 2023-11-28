# TNM: A Tool for Mining of Socio-Technical Data from Git Repositories

TNM is an open-source tool for mining socio-technical data from Git repositories and vizualizing it.
Instead of implementing their own mining pipeline, researchers can use our tool or integrate it in their own mining pipelines.

## About

TNM incorporates implementations of several established data mining techniques, or individual miners. Every class implements one interface or abstract class for each task type, which makes it easily extendable.

Base classes:
* `Mapper` - assign unique IDs to specific entities, e.g., users.
* `Miner` - mine data from various sources for one task.
* [`GitMiner`](#Git+miners) - extend the `Miner` interface. Use specifically Git repositories as the source of data.
* `DataProcessor` - process mined data. Work as a buffer for `Miner` classes.
* [`Calculation`](#Calculation) - calculate complex dependencies in data. Use data from GitMiners.
* [`Visualization`](#Visualization) - visualize processed data.

### Git miners

**Git miners** are classes implementing mining tasks. 
All miners use a local Git repository for data extraction and extend the abstract class `GitMiner` with two functions to  process the commit history in chosen branches and save the results. 

The current version of TNM includes the following `GitMiner` implementations:
* `FilesOwnershipMiner` is based on the Degree of Knowledge (DOK) 
  ([paper](https://lib.dr.iastate.edu/cgi/viewcontent.cgi?article=5670&context=etd)). 
  DOK quantifies the knowledge of a developer or a group of developers about a particular section of code.
  The miner yields a knowledge score for every developer to a file pair in the form of a nested map. 
  It also extracts information about the authorship of code on a line level.
  
* `CommitInfluenceGraphMiner` is based on an application of the PageRank to commits 
  ([paper](https://ieeexplore.ieee.org/document/8051375)).
  It finds bug-fixing commits by searching for `fix` in commit messages. Then, using ***git blame***, the miner finds previous commits in which the lines changed in the fix commit had been introduced. The output of the miner is a map of lists, with keys corresponding to fixing commit IDs and values corresponding to commits introducing the lines changed by the fixes.
  
* `AssignmentMatrixMiner` yields a modification count for each developer to a file pair in the form of a nested map. The result is used to calculate socio-technical congruence 
  ([paper](https://ieeexplore.ieee.org/abstract/document/5740929)).  
  
* `FileDependencyMatrixMiner` processes commits to find the files that were changed in the same commit.
  For each pair of files, the miner yields a number of times they have been edited in the same commit.
  This data can be utilized to build the edges of the socio-technical software network based on Conway's law
  ([paper](https://ieeexplore.ieee.org/abstract/document/4228662)) and calculation of socio-technical congruence.
  ([paper](https://ieeexplore.ieee.org/abstract/document/5740929)).

* `CoEditNetworksMiner` is based on git2net ([github](https://github.com/gotec/git2net), 
  [paper](https://dl.acm.org/doi/10.1109/MSR.2019.00070)). Yields a JSON file with a dict of commits information and a list of edits.
  Each edit includes pre/post file path, start line, length, number of chars, entropy of the changed block of code, 
  Levenshtein distance between the previous and new block of code, type of edit.
  
* `ComplexityCodeChangesMiner` is based on [paper](https://sail.cs.queensu.ca/Downloads/ICSE2009_PredictingFaultsUsingTheComplexityOfCodeChanges.pdf).
  Yields a JSON file with a dict of periods that includes the period's entropy and the stats of files changed in that period.
  Each file stat includes entropy and History Complexity Period Factors, such as HCPF2 and HCPF3.
  
* `WorkTimeMiner` is a simple miner for mining the distribution of commits over time in the week.
  This data can be used, e.g., to improve work scheduling by finding intersections in the time distributions between different developers.
  
* `UserChangedFilesMiner` is a simple miner for mining sets of changed files for each developer. It can be used, for example, to count how many times a certain file was edited by specific developers.

### Calculation
Some forms of data require non-trivial computations. To ensure extensibility, processing code is separated from the miners into dedicated classes.

* `CoordinationNeedsMatrixCalculation` computes the coordination needs matrix according to the 
  algorithm of [paper](https://ieeexplore.ieee.org/abstract/document/5740929), using the data obtained 
  by `FileDependencyMatrixMiner` and `AssignmentMatrixMiner`. The computation results are represented as a 
  matrix ***C[i][j]***, where ***i, j*** are developer user IDs, and ***C[i][j]*** is the relative coordination 
  need between the two individuals.
  
* `MirrorCongruenceCalculation` computes the socio-technical congruence according to 
  [paper](https://ieeexplore.ieee.org/abstract/document/4228662).
  Its output is a single number in the **[0, 1]** range with higher values corresponding to higher socio-technical congruence.
  
* `PageRankCalculation` computes a PageRank vector according to the algorithm of Suzuki et al. 
  [paper](https://ieeexplore.ieee.org/document/8051375). A PageRank vector contains importance rankings for each commit.
  The input data for `PageRankCalculation` is the commit influence graph produced by the `CommitInfluenceGraphMiner`.
  The output is a vector where each element represents the importance of a commit.

### Visualization

TNM includes a basic browser-based visualization class `WeightedEdgesGraphHTML` for the output of 
`FileDependencyMatrixMiner` and `CoordinationNeedsMatrixCalculation`.

## Usage

### CLI
1. Run `./gradlew :cli:shadowJar`
2. Now you can use shell script to use cli `./run.sh`

The script should be executed as:    
```shell script
./run.sh commandName options arguments
```

When run without arguments, `run.sh` shows all available commands. Also, you can call `./run.sh commandName -h` 
to get information about necessary options and arguments.

Example of script usage: 
```shell script
./run.sh AssignmentMatrixMiner --repository ./local_repository/.git main
```


### API library

Modify `build.gradle.kts`

```gradle

repositories {
  maven {
    url = uri("https://packages.jetbrains.team/maven/p/ictl-public/public-maven")
  }
}

dependencies {
  implementation("org.jetbrains.research.ictl:tnm:0.4.16")
}
```

#### Miner usage example

```kotlin
val localGitPath = "./your_repository_dir/.git"
val repository = FileRepository(localGitPath)
val numThreads = 4
val branches = setOf("main", "dev")

val dataProcessor = WorkTimeDataProcessor()
val miner = WorkTimeMiner(repository, branches, numThreads = numThreads)
miner.run(dataProcessor)

val resultFile = File("./path_where_to_store_results")
val idToUserFile = File("./path_where_to_store_idToUser")

HelpFunctionsUtil.saveToJson(
  resultFile,
  dataProcessor.workTimeDistribution
)

HelpFunctionsUtil.saveToJson(
  idToUserFile,
  dataProcessor.idToUser
)
```

#### Calculation usage example

```kotlin
val repository = FileRepository(repositoryDirectory)
val numThreads = 4
val branches = setOf("main", "dev")

val dataProcessor = CommitInfluenceGraphDataProcessor()
val miner = CommitInfluenceGraphMiner(repository, branches, numThreads = numThreads)
miner.run(dataProcessor)

val resultFile = File("./path_where_to_store_results")
val idToCommitFile = File("./path_where_to_store_idToUser")

HelpFunctionsUtil.saveToJson(
  resultFile,
  dataProcessor.adjacencyMap
)

HelpFunctionsUtil.saveToJson(
  idToCommitFile,
  dataProcessor.idToCommit
)
```

### Output format

Miners, calculation and mapper classes use the JSON output format. JSON is easy to read; objects (such as hash maps and arrays) serialized in the JSON format can be deserialized in other programming languages. Visualization classes generate an interactive HTML graph which can be viewed in any modern web browser and shared without worrying about the dependencies. The graph can also be edited manually to adjust its appearance if required.

## Extend

### Example of implementing one's own technique

```kotlin
// Mark processing data with marker interface InputData
data class UserName(val email: String) : InputData

// Extend data processor
class MyDataProcessor : DataProcessorMapped<UserName>() {
    // Using Java Concurrent package for storing results
    private val _result = ConcurrentSkipListSet<Int>()
    // Backing field for immutable public field
    val result : Set<Int>
        get() = _result

    override fun processData(data: UserName) {
        val userId = userMapper.add(data.email)
        _result.add(userId)
    }

    override fun calculate() {
      println("Calculation called!")
    }
}

// Extend GitMiner and override function [process]
class MyGitMiner(
  repository: File,
  neededBranches: Set<String>,
  numThreads: Int = ProjectConfig.DEFAULT_NUM_THREADS
) : GitMiner<MyDataProcessor>(repository, neededBranches, numThreads = numThreads) {
  override fun process(dataProcessor: MyDataProcessor, commit: RevCommit) {
    val data = UserName(commit.authorIdent.emailAddress)
    dataProcessor.processData(data)
  }
}

fun main() {
  val repository = File("./.git")
  val branches = setOf("main")
  val numThreads = 4

  val miner = MyGitMiner(repository, branches, numThreads)
    val dataProcessor = MyDataProcessor()
    miner.run(dataProcessor)

    println(dataProcessor.result)
}
```

### FAQ

#### I got data not from a Git repository. How can I use implemented techniques?

You can create your own miner class by extending the `Miner` interface with the necessary `DataProcessor` as a generic
type. Then all you need to do is iteratively transmit data to `DataProcessor` in method `run(dataProcessor: T)`

#### I don't know how to work with multithreading. Is there any way I can contribute my technique?

Yes, you can! Use the example above and when you extend `GitMiner`, set the parameter `numThreads` to 1. Also, you don't
need to use the Java Concurrent package for storing your results.

```kotlin
class MyGitMiner(
    repository: FileRepository,
    neededBranches: Set<String>
) : GitMiner<MyDataProcessor>(repository, neededBranches, numThreads = 1) {
    // ...
}
```

