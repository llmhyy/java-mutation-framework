# java-mutation-framework

Mutates a given maven or gradle project to create artificial bugs in the code for debugging research.<br/>
It first runs a test case and collects its trace using Microbat's instrumentator.jar.<br/>
It clones the project and mutates portions of the code that is covered by the test case. It then reruns the test case and returns the trace of the mutated test case.

## Set-up
First install
- java 8
- java 11 (or above)
- Maven or Gradle (based on the project to mutate)

Java 8 is needed to run the trace collection jar file (microbat's instrumentator.jar) and java 11 (or above) is required to compile the AST Node dependency used in this project (Eclipse JDT API)

- Set maven/gradle in the environment variables.
- Set the IDE settings to use java 11 (or above)

The project utilises the trace-model project as a submodule.
To set up trace-model:
- Run ```git submodule update --init --recursive```
- Enter trace-model directory
- Run ```mvn install``` to install the trace-model into your local maven repository. 

To complete the set up:
- Enter root directory and run ```mvn compile```
- Modify the ```java_home``` argument in the ./sampleMicrobatConfig.json file to the path to java 8.
- (Optional) If you are familiar with Microbat's configuration, the other options can also be configured in the json file.

To test if it is working, run ```jmutation.Main```
with ```-projectPath ./sample/math_70 -dropInsDir ./lib -microbatConfig ./sampleMicrobatConfig.json``` as arguments.<br/>
It should clone the project into `<temp directory>/mutation`, mutate the project, and run trace collection on the same test case.

## Set-up (External library)
After completing the above steps, ```mvn package``` can be used to form a single jar file for use as an external library.

## Usage
As an external library, the public methods in jmutation.MutationFramework should be used. An example usage is provided in the trace-manager project. (See tracemanager.TraceManager in https://github.com/bchenghi/trace-manager). <br/>
Java docs are also available in the ./docs directory.

From the command line, the jar file can be executed with the following parameters:
- projectPath (path to project, required)
- dropInsDir (path to microbat jar files, required)
- microbatConfig (path to microbat configuration json file)
 
## MicrobatConfig
It is specified in a json file, with the option name as key, and its values as an array. An example is provided in `sampleMicrobatConfig.json`.
Only certain configurations are modifiable for microbat as the rest has to be overwritten during runtime.
- java_home
- log
- run_id
- stepLimit
- trace_recorder (in development)

## How it works (Developers)

The project is run from `jmutation.Main`. It passes the project path and microbat jar files directory (dropInsDir)
into `ProjectConfig` class. It parses the project and obtains the test cases. 

It checks for gradle files to determine that it is a gradle project, otherwise, it is maven. 

Microbat configuration is obtained by parsing a json file. Path to a json file can be 
specified. `jmutation.model.MicrobatConfig` object is created, which contains the default configuration, with options specified in the json overwriting the default values.
The microbat configuration and project configuration is passed into a `jmutation.execution.ProjectExecutor` object
and `ProjectExecutor#execPrecheck` method is called with a test case.
`jmutation.execution.InstrumentationCommandBuilder` is created which creates the microbat command to run.

`ProjectExecutor#precheckExec` method generates the microbat precheck command to run Junit test case, runs it, and collect diagnostic information as well as the test coverage (e.g. step over limit)

For mutation, the project is cloned into the `<temp directory>/mutation` directory. Using the test coverage, mutation ranges are obtained, and the cloned project is mutated.
Precheck is run on the mutated project for diagnostic.

The traces for both original and mutated projects are obtained. `ProjectExecutor#exec` is called with a test case, and a similar process to precheck occurs.
`jmutation.trace.FileReader` is used to read the trace.

Trace collection is done in `jmutation.trace.FileReader`.
It contains a `jmutation.trace.TraceInputStream` object, which reads from a trace file and forms a list of traces. It works the same as `microbat.instrumentation.output.TraceOutputReader#readTrace`.

The root causes are obtained by using the mutation history from the Mutator, and checking the buggy trace for TraceNodes that overlap in line numbers and class name stored in each `jmutation.mutation.commands.MutationCommand` objects.

The various results such as the InstrumentationResults of the buggy and non-buggy traces, the 2 project roots, root causes and whether the buggy test case has passed is stored in a MutationResult object, which is returned from the `jmutation.MutationFramework#startMutationFramework` method.