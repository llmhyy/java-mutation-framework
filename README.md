# java-mutation-framework

The project is run from `jmutation.Main`. It passes the project path and microbat jar file directory (dropInsDir) into `ProjectConfig` class. It parses the project and obtains the test cases. Currently it assumes it is maven.

Microbat configuration is obtained by parsing a json file. Path to a json file is specified. `jmutation.model.MicrobatConfig` object is created, which contains the configuration. 
The microbat configuration and project configuration is passed into a `jmutation.execution.ProjectExecutor` object and `ProjectExecutor#exec` method is called with a test case.
InstrumentationCommandBuilder is created which creates the microbat command to run.

`ProjectExecutor#instrumentationExec` generates the microbat command to run Junit test case with `MicrobatTestRunner`, runs it, and uses `jmutation.trace.FileReader` to read the trace. 

Trace collection is done in `jmutation.trace.FileReader`.
It contains a `jmutation.trace.TraceInputStream` object, which reads from a trace file and forms a list of traces. It works the same as `microbat.instrumentation.output.TraceOutputReader#readTrace`.

## Current issues
Currently, facing issues running `MicrobatTestRunner` locally. Also unsure of how mutation will be carried out with the given list of traces and how the mutated trace can be recompiled and run again.