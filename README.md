# TNM
A tool and library for mining several types of data from git
repositories. 
Instead of implementing their own mining pipeline researchers can use
our tool or integrate it in their own mining pipelines.

## About

The main idea behind the structure of the tool is that every class implement one
interface or abstract class for each task type, which make it easily extendable.

Currently, tool got 4 types of classes:
* Mapper - singleton classes for mapping specified entity for the unique id.
* GitMiner - classes for mining data for one task.
* Calculation - classes for calculating complex dependencies in data.
  Use data from GitMiners.
* Visualization - classes for visualization proceed data.

### Git miners

**Git miners** are classes implementing the mining tasks. 
All miners use a local git repository for data extraction and extend abstract class 
`GitMiner` with two functions to  process  commit history  in  chosen  branches  and  save  the results. 
The current  version  of TNM  includes following `GitMiner` implementations:
* `FilesOwnershipMiner` is based on Degree of Knowledge (DOK) 
  ([paper](https://lib.dr.iastate.edu/cgi/viewcontent.cgi?article=5670&context=etd)). 
  DOK quantifies the knowledge of a   developer or set of developers about a particular section of code.
  The miner yields a knowledge score for every developer to file pair in the form of a nested map. 
  It also yields information about authorship of code on a line level.
  
* `CommitInfluenceGraphMiner` is based on application of PageRank to commits 
  ([paper](https://ieeexplore.ieee.org/document/8051375)).
  It finds the bug-fixing commits by searching for `fix` in commit 
  messages. Then, using ***git blame*** the miner finds the commits previously modifying 
  the lines changed by the fix commit. The output of the miner is a map of lists, with 
  keys corresponding to fixing commit ids and values corresponding to the 
  commits introducing the lines changed by the fixes.
  
* `AssignmentMatrixMiner` yields a modification count for each developer to file 
  pair in the form of a nested map. Result is used for calculation of socio-technical congruence 
  ([paper](https://ieeexplore.ieee.org/abstract/document/5740929)).  
  
* `FileDependencyMatrixMiner` processes the commits to find the files that were changed in the same commit.
  For each pair of files, the miner yields a number of times they have been edited in the same commit.
  This data can be utilized to build the edges of the socio-tecnhical software network based on Conway's law
  ([paper](https://ieeexplore.ieee.org/abstract/document/4228662)) and calculation of socio-technical congruence.
  ([paper](https://ieeexplore.ieee.org/abstract/document/5740929)).

* `CoEditNetworksMiner` is based on git2net ([github](https://github.com/gotec/git2net), 
  [paper](https://dl.acm.org/doi/10.1109/MSR.2019.00070)). Yields JSON file with dict of commits information and list of edits.
  Each edit includes pre/post file path, start line, length, number of chars, entropy of changed block of code, 
  Levenshtein distance between previous and new block of code, type of edit.
  
* `WorkTimeMiner` is a simple miner for mining the distribution of commits over time in the week.
  This data can be used to ***e.g.*** improve work scheduling by finding intersections in the time distributions 
  between different developers.
  
* `ChangedFilesMiner` is a simple miner for mining sets of changed files for each developer. It can be used, 
  for example, for deriving sets of files edited in common between multiple developers.

### Calculation
Some forms of data require non-trivial computations. To ensure extensibility, processing code is 
separated from the miners into dedicated classes.

* `CoordinationNeedsMatrixCalculation` computes the coordination needs matrix according to the 
  algorithm of [paper](https://ieeexplore.ieee.org/abstract/document/5740929), using the data obtained 
  by `FileDependencyMatrixMiner` and `AssignmentMatrixMiner`. The computation results are represented as a 
  matrix ***C[i][j]***, where ***i, j*** are the developers user ids, and ***C[i][j]*** is the relative coordination 
  need between the two individuals.
  
* `MirrorCongruenceCalculation` computes the socio-technical congruence according to 
  [paper](https://ieeexplore.ieee.org/abstract/document/4228662), using the data yielded by `CoordinationNeedsMatrixCalculation`.
  Its output is a single number in the **[0, 1]** range with higher values corresponding to higher socio-technical congruence.
  
* `PageRankCalculation` computes PageRank vector according to algorithm of Suzuki et al. 
  [paper](https://ieeexplore.ieee.org/document/8051375). PageRank vector contains importance rankings for each commit.
  The input data for the `PageRankCalculation` is the commit influence graph, produced by the `CommitInfluenceGraphMiner`.
  The output is a vector where each element represents importance of a commit.

### Visualization

TNM includes basic browser-based visualization class `WeightedEdgesGraphHTML` for output of 
`FileDependencyMatrixMiner` and `CoordinationNeedsMatrixCalculation`.

## Usage

### CLI
1. Run `./gradlew shadowJar`
2. Now you can use shell script to use cli `./run.sh` 

Script should be executed as:    
```shell script
./run.sh commandName options arguments
```

By running `run.sh` without arguments it will show all
available commands. Also, you can call `./run.sh commandName -h` 
to get information about needed `options` and `arguments`.

Example of script usage: 
```shell script
./run.sh AssignmentMatrixMiner --repository ./local_repository/.git --resources ./resources main
```


### API library

#### Miner usage example

```kotlin
val localGitPath = "./your_repository_dir/.git"
val repository = FileRepository(localGitPath)

val miner = WorkTimeMiner(repository)
miner.run()

val resultPath = "./path_where_to_store_results"
miner.saveToJson(File(resultPath))
```

#### Calculation usage example

```kotlin
val resourceDirectory = File(resourceDirectory)

// executed and saved results to same resourceDirectory
// of AssignmentMatrixMiner and FileDependencyMatrixMiner

val calculation = CoordinationNeedsMatrixCalculation(resourceDirectory)
calculation.run()
calculation.saveToJson(resourceDirectory)
```
    

### Output format
Miners, calculation and mapper classes use a JSON output format.
JSON is easy to read and objects (such as hash maps and arrays) 
serialized in the JSON format can be deserialized in another programming languages.
Visualization classes generate interactive html graph which can be viewed 
in any modern web browser and shared without worrying about dependencies. 
The graph can be also edited manually to adjust its appearance if required. 

