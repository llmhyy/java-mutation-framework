# java-mutation-framework

Mutates a given maven or gradle project to create artificial bugs in the code for debugging research.<br/>
It first runs a test case and collects its trace using Microbat's instrumentator.jar.<br/>
It clones the project and mutates portions of the code that is covered by the test case. It then reruns the test case
and returns the trace of the mutated test case.

Follow the set-up and usage sections below to use this project.

## Set-up

1. install
    - Java 8
    - Java 11 (or above)
    - Maven
2. Set `JAVA_HOME` environment variable to path to Java 11 (or above)
3. Run `.\scripts\setup.bat`
4. Enter `%USERPROFILE%\lib\resources\java-mutation-framework\microbatConfig.json` file and modify `java_home` argument
   to the path to Java 8
5.

### How the set-up works (Developers)

1. Why are both Java 8 and Java 11 (or above) required?<br/>
   Java 8 is needed to run the trace collection jar file (microbat's instrumentator.jar)<br/>
   Java 11 (or above) is required to compile the AST Node dependency used in this project (Eclipse JDT API).<br/>
   Thus, to compile this project using Maven, `JAVA_HOME` is set to Java 11. If an IDE is used to run Maven, Java 11 can
   be set in the IDE as well.<br/>
   Java 8 is set in the `microbatConfig.json` file, to run `instrumentator.jar` for trace collection.

2. What does `.\scripts\setup.bat` do?<br/>
   The project requires some external jar files to be run as a separate process, as well as the `microbatConfig.json`
   file
   to run `instrumentator.jar`. Thus, they must be copy-pasted to a system-defined location for this project to locate
   them.<br/>
   The script copy-pastes the files in `.\src\main\resources`
   to `%USERPROFILE%\lib\resources\java-mutation-framework`

To test if it is working,

1. Ensure Java 8 path is correct
   in `%USERPROFILE%\lib\resources\java-mutation-framework\microbatConfig.json`
2. Run ```jmutation.Main```
   with ```-projectPath ./sample/math_70 -testCase org.apache.commons.math.analysis.BinaryFunctionTest#testAdd``` as
   arguments.<br/>

It should clone the project into `%Temp%/mutation`, mutate the project, and run trace collection on the same test
case.

## Usage

The jar file is available at `.\target\java-mutation-framework-0.0.1-SNAPSHOT-jar-with-dependencies.jar` after
setting-up.<br/>
As an external library, the public methods in `jmutation.MutationFramework` should be used. An example usage is provided
in the trace-manager project. (See tracemanager.TraceManager in https://github.com/bchenghi/trace-manager). <br/>
Java docs are also available in the ./docs directory.

From the command line, the jar file can be executed with the following parameters:

- projectPath (path to project, required)
- testCase (test case to mutate and collect coverages, required)
- dropInsDir (path to instrumentation dependencies)
- microbatConfig (path to MicroBat configuration JSON file)

## MicrobatConfig

It is specified in a json file, with the option name as key, and its values as an array. An example is provided
in `sampleMicrobatConfig.json`.
Only certain configurations are modifiable for microbat as the rest has to be overwritten during runtime.

- java_home
- log
- run_id
- stepLimit

## How it works (Developers)

The project is run from `jmutation.Main`. It passes the project path and microbat jar files directory (dropInsDir)
into `ProjectConfig` class. It parses the project and obtains the test cases.

It checks for gradle files to determine that it is a gradle project, otherwise, it is maven.

Microbat configuration is obtained by parsing a json file. Path to a json file can be
specified. `jmutation.model.MicrobatConfig` object is created, which contains the default configuration, with options
specified in the json overwriting the default values.
The microbat configuration and project configuration is passed into a `jmutation.execution.ProjectExecutor` object
and `ProjectExecutor#execPrecheck` method is called with a test case.
`jmutation.execution.InstrumentationCommandBuilder` is created which creates the microbat command to run.

`ProjectExecutor#precheckExec` method generates the microbat precheck command which runs the Junit test case, and
collects diagnostic information (e.g. step over limit) as well as the test coverage. The command is then executed.

The project is then cloned into the `<temp directory>/mutation` directory. Using the test coverage, possible mutation
ranges are obtained, and the cloned project is mutated.
Precheck is also run on the mutated project for diagnostic.

The traces for both original and mutated projects are then obtained.
`ProjectExecutor#exec` is called with a test case, which will run the test case, and collects a trace.
The trace is read using `microbat.instrumentation.output.RunningInfo#readFromFile`.

The root causes are obtained by using the mutation history from the Mutator, and checking the buggy trace for TraceNodes
that overlap in line numbers and class name stored in each `jmutation.mutation.MutationCommand` objects.

The various results such as the InstrumentationResults of the buggy and non-buggy traces, the 2 project roots, root
causes and whether the buggy test case has passed is stored in a MutationResult object, which is returned from
the `jmutation.MutationFramework#startMutationFramework` method.

## To update `instrumentator.jar`

`instrumentator.jar` or other external libraries may require updating. Follow the steps to update them in the project's
local repository.

- Overwrite the `instrumentator.jar` in the `src/main/resources/lib` directory
- Run:
    - `mvn install:install-file -Dfile=src/main/resources/lib/instrumentator.jar -DgroupId=microbat -DartifactId=instrumentator -Dversion=0.0.1 -Dpackaging=jar -DgeneratePom=true -DlocalRepositoryPath=lib -DcreateChecksum=true -U`
    - `mvn dependency:purge-local-repository -DactTransitively=false`
    - `mvn -DskipTests=true install`
- Update the `instrumentator.jar` in the `%USERPROFILE%\lib\resources\java-mutation-framework\lib` directory
