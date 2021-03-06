# java-mutation-framework

## Set up

Install java 8 and mvn.
Set ```mvn``` and ```JAVA_HOME``` in the environment variables.

The project utilises the trace-model project as a submodule.
To set up trace-model:
- Run ```git submodule update --init --recursive```
- Enter trace-model directory
- Run ```mvn install``` to install the trace-model into your local maven repository. 

To test if it is working, run ```jmutation.Main```
with ```-projectPath ./sample/math_70 -dropInsDir ./lib -microbatConfig ./sampleMicrobatConfig.json``` as arguments.

## How it works

The project is run from `jmutation.Main`. It passes the project path and microbat jar file directory (dropInsDir)
into `ProjectConfig` class. It parses the project and obtains the test cases. Currently it assumes it is maven.

Microbat configuration is obtained by parsing a json file. Path to a json file is
specified. `jmutation.model.MicrobatConfig` object is created, which contains the configuration.
The microbat configuration and project configuration is passed into a `jmutation.execution.ProjectExecutor` object
and `ProjectExecutor#exec` method is called with a test case.
`jmutation.execution.InstrumentationCommandBuilder` is created which creates the microbat command to run.

`ProjectExecutor#instrumentationExec` method generates the microbat command to run Junit test case, runs it, and
uses `jmutation.trace.FileReader` to read the trace.

Trace collection is done in `jmutation.trace.FileReader`.
It contains a `jmutation.trace.TraceInputStream` object, which reads from a trace file and forms a list of traces. It
works the same as `microbat.instrumentation.output.TraceOutputReader#readTrace`.

## Notes

As some of the Microbat configurations will change during this project's run (e.g. working directory of original project
is different from clone used for mutation) they should not be specified in the Microbat configuration as this will cause
it to be fixed throughout the run. The sampleMicrobatConfig.json file in the root contains the fields that can be fixed
by the user. However, those fields will also be auto-generated with default values if they are not specified.
