package jmutation.execution;

import jmutation.model.MicrobatConfig;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstrumentationCommandBuilderTest {
    @Test
    void generateCommand_validMicrobatConfig_generatesCorrectCommand() {
        String validConfigFilePath = "src/test/files/jmutation/execution/validMicrobatConfigTest.json";
        String projectPath = "./project";
        MicrobatConfig microbatConfig = MicrobatConfig.parse(validConfigFilePath, projectPath);
        String dropInsDir = "./src/main/resources/lib";
        InstrumentationCommandBuilder instrumentationCommandBuilder = new InstrumentationCommandBuilder(microbatConfig, dropInsDir);
        String actualCommand = instrumentationCommandBuilder.generateCommand();
        MicrobatConfig updatedMicrobatConfig = microbatConfig.setClassPaths(instrumentationCommandBuilder.classPaths);
        File instrumentatorFile = new File(dropInsDir + "/instrumentator.jar");
        String instrumentatorFilePath = instrumentatorFile.getAbsolutePath();
        String expectedCommand = "\"C:\\java\"" + File.separator + "bin" + File.separator +
                "java -Xmx10G -XX:+UseG1GC -ea -noverify -javaagent:" + instrumentatorFilePath + "=" +
                updatedMicrobatConfig + " -cp " + updatedMicrobatConfig.getClassPathStr();
        assertEquals(expectedCommand, actualCommand);
    }

    @Test
    void generateCommand_validMicrobatConfigAndDebugModeOn_generatesCorrectCommand() {
        String validConfigFilePath = "src/test/files/jmutation/execution/validMicrobatConfigTest.json";
        String projectPath = "./project";
        MicrobatConfig microbatConfig = MicrobatConfig.parse(validConfigFilePath, projectPath);
        String dropInsDir = "./src/main/resources/lib";
        InstrumentationCommandBuilder instrumentationCommandBuilder = new InstrumentationCommandBuilder(microbatConfig, dropInsDir);
        instrumentationCommandBuilder.setDebugMode(true);
        String actualCommand = instrumentationCommandBuilder.generateCommand();
        MicrobatConfig updatedMicrobatConfig = microbatConfig.setClassPaths(instrumentationCommandBuilder.classPaths);
        File instrumentatorFile = new File(dropInsDir + "/instrumentator.jar");
        String instrumentatorFilePath = instrumentatorFile.getAbsolutePath();
        String expectedCommand = "\"C:\\java\"" + File.separator + "bin" + File.separator +
                "java -agentlib:jdwp=transport=dt_socket,address=9001,server=y,suspend=y " +
                "-Xmx10G -XX:+UseG1GC -ea -noverify -javaagent:" + instrumentatorFilePath + "=" +
                updatedMicrobatConfig + " -cp " + updatedMicrobatConfig.getClassPathStr();
        assertEquals(expectedCommand, actualCommand);
    }
}
