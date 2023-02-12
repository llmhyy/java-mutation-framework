package jmutation.execution;

import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutorTest {
    private File fileToDelete = new File("");

    @Test
    void writeCmdToTempBatFile_DummyCmd_CreatesBatFile(@TempDir File tempDir) throws IOException {
        Executor executor = new Executor(tempDir);
        String dummyCmd = "dummy cmd";
        File batFile = executor.writeCmdToTempBatFile(dummyCmd);
        fileToDelete = batFile;
        assertTrue(FileUtils.getExtension(batFile.getName()).equals("bat"));
        assertEquals(dummyCmd, Files.readString(batFile.toPath()));
    }

    @AfterEach
    void tearDown() {
        fileToDelete.delete();
    }
}