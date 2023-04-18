package jmutation.execution;

import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutorTest {
    private File fileToDelete = new File("");
    @TempDir
    private File tempDir;
    private Executor executor;

    @BeforeEach
    void setup() {
        executor = new Executor(tempDir);
    }

    @AfterEach
    void tearDown() {
        fileToDelete.delete();
    }

    @Test
    void exec_normalCmd_ExecutesSuccessfully() {
        String cmdThatDoesNothing = "cd .";
        assertDoesNotThrow(() -> executor.exec(cmdThatDoesNothing));
    }
}