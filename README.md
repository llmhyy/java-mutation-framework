# java-mutation-framework

Mutates a given maven or gradle project to create artificial bugs in the code for debugging research.<br/>
It first runs a test case and collects its trace using Microbat's instrumentator.jar.<br/>
It clones the project and mutates portions of the code that is covered by the test case. It then reruns the test case
and returns the trace of the mutated test case.

## Set-up

1. install
    - Java 8
    - Java 11 (or above)
    - Maven
2. Set `JAVA_HOME` environment variable to path to Java 11 (or above)
3. Run `.\scripts\setup.bat`
4. Enter `%USERPROFILE%\lib\resources\java-mutation-framework\microbatConfig.json` file and modify `java_home` argument
   to the path to Java 8

### How the set-up works (Developers)

1. Why are both Java 8 and Java 11 (or above) required?<br/>
   Java 8 is needed to run the trace collection jar file (microbat's instrumentator.jar)<br/>
   Java 11 (or above) is required to compile the AST Node dependency used in this project (Eclipse JDT API).<br/>
   Thus, to compile this project using Maven, `JAVA_HOME` is set to Java 11. If an IDE is used to run Maven, Java 11 can
   be set in the IDE as well.<br/>
   Java 8 is set in the `microbatConfig.json` file, to run `instrumentator.jar` for trace collection.

2. What does `.\scripts\setup.bat` do?<br/>
   The project utilises the trace-model project as a submodule. We must install it into the local maven repository for
   packaging this project.<br/>
   The project also requires some external libraries in the form of jar files, as well as the `microbatConfig.json` file
   to run `instrumentator.jar`. Thus, they must be copy-pasted to a system-defined location for this project to locate
   them.<br/>
    1. Run ```git submodule update --init --recursive```
    2. Enter trace-model directory
    3. Run ```mvn install``` to install the trace-model into your local maven repository.<br/>
    4. Copy-pastes `.\resources\lib` and `.\resources\microbatConfig.json`
       to `%USERPROFILE%\lib\resources\java-mutation-framework`

To test if it is working, run ```jmutation.Main```
with ```-projectPath ./sample/math_70 -dropInsDir ./lib -microbatConfig ./sampleMicrobatConfig.json``` as
arguments.<br/>
It should clone the project into `%Temp%/mutation`, mutate the project, and run trace collection on the same test case.

## Usage

The jar file is available at `.\target\java-mutation-framework-0.0.1-SNAPSHOT-jar-with-dependencies.jar` after
setting-up.<br/>
As an external library, the public methods in `jmutation.MutationFramework` should be used. An example usage is provided
in the trace-manager project. (See tracemanager.TraceManager in https://github.com/bchenghi/trace-manager). <br/>
Java docs are also available in the ./docs directory.

From the command line, the jar file can be executed with the following parameters:

- projectPath (path to project, required)
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
- trace_recorder (in development)

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

`ProjectExecutor#precheckExec` method generates the microbat precheck command to run Junit test case, runs it, and
collect diagnostic information as well as the test coverage (e.g. step over limit)

For mutation, the project is cloned into the `<temp directory>/mutation` directory. Using the test coverage, mutation
ranges are obtained, and the cloned project is mutated.
Precheck is run on the mutated project for diagnostic.

The traces for both original and mutated projects are obtained. `ProjectExecutor#exec` is called with a test case, and a
similar process to precheck occurs.
`jmutation.trace.FileReader` is used to read the trace.

Trace collection is done in `jmutation.trace.FileReader`.
It contains a `jmutation.trace.TraceInputStream` object, which reads from a trace file and forms a list of traces. It
works the same as `microbat.instrumentation.output.TraceOutputReader#readTrace`.

The root causes are obtained by using the mutation history from the Mutator, and checking the buggy trace for TraceNodes
that overlap in line numbers and class name stored in each `jmutation.mutation.MutationCommand` objects.

The various results such as the InstrumentationResults of the buggy and non-buggy traces, the 2 project roots, root
causes and whether the buggy test case has passed is stored in a MutationResult object, which is returned from
the `jmutation.MutationFramework#startMutationFramework` method.