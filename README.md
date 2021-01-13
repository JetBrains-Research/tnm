# XXX
A library for mining several types of data from git
repositories. The value of tool is in the reduction of the development
effort for extracting socio-technical data for the further analysis.
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
val localGitPath = "your_repository_dir/.git"
val repository = FileRepository(localGitPath)

val miner = WorkTimeMiner(repository)
miner.run()

val resultPath = "path_where_to_store_results"
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

