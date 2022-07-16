package jmutation.execution;

import jmutation.model.MicrobatConfig;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InstrumentationCommandBuilderTest {
    @Test
    void generateCommand_validMicrobatConfig_generatesCorrectCommand() {
        String validConfigFilePath = "src/test/files/jmutation/execution/validMicrobatConfigTest.json";
        String projectPath = "./project";
        MicrobatConfig microbatConfig = MicrobatConfig.parse(validConfigFilePath, projectPath);
        String dropInsDir = "./lib";
        InstrumentationCommandBuilder instrumentationCommandBuilder = new InstrumentationCommandBuilder(microbatConfig, dropInsDir);
        String actualCommand = instrumentationCommandBuilder.generateCommand();
        MicrobatConfig updatedMicrobatConfig = microbatConfig.setClassPaths(instrumentationCommandBuilder.classPaths);
        File instrumentatorFile = new File("lib/instrumentator.jar");
        String instrumentatorFilePath = instrumentatorFile.getAbsolutePath();
        String expectedCommand = "\"C:\\java\"" + File.separator + "bin" + File.separator + "java -Xmx10G -XX:+UseG1GC -ea -noverify -javaagent:" + instrumentatorFilePath + "=" + updatedMicrobatConfig + " -cp " + updatedMicrobatConfig.getClassPathStr();
        assertEquals(expectedCommand, actualCommand);
    }
}
