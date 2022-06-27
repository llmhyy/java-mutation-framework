package jmutation.execution;

import jmutation.compile.execution.InstrumentationCommandBuilder;
import jmutation.model.MicrobatConfig;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class InstrumentationCommandBuilderTest {
    @Test
    void generateCommand_validMicrobatConfig_generatesCorrectCommand() {
        String validConfigFilePath = "src/test/files/jmutation/execution/validMicrobatConfigTest.json";
        MicrobatConfig microbatConfig = MicrobatConfig.parse(validConfigFilePath);
        String dropInsDir = "./lib";
        InstrumentationCommandBuilder instrumentationCommandBuilder = new InstrumentationCommandBuilder(microbatConfig, dropInsDir);
        String actualCommand = instrumentationCommandBuilder.generateCommand();
        MicrobatConfig updatedMicrobatConfig = microbatConfig.setClassPaths(instrumentationCommandBuilder.classPaths);
        File instrumentatorFile = new File("lib/instrumentator.jar");
        String instrumentatorFilePath = instrumentatorFile.getAbsolutePath();
        String expectedCommand = "C:\\java\\bin\\java -Xmx10G -XX:+UseG1GC -ea -noverify -javaagent:" + instrumentatorFilePath + "=" + updatedMicrobatConfig + " -cp " + updatedMicrobatConfig.getClassPathStr();
        assertEquals(expectedCommand, actualCommand);
    }
}
