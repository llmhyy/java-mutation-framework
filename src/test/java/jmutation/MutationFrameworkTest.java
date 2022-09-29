package jmutation;

import jmutation.constants.ExternalLibrary;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class MutationFrameworkTest {
    private String tempDirPath = "./src/test/temp";
    private File tempDir = new File(tempDirPath);

    @Test
    public void extractResources_provideDirectory_extractsAllIntoDirectory() throws IOException {
        MutationFramework mf = new MutationFramework();
        mf.extractResources(tempDirPath);
        for (ExternalLibrary externalLibrary : ExternalLibrary.values()) {
            assert(new File(tempDir, "lib" + File.separator + externalLibrary.getName() + ".jar").exists());
        }
        assert(new File(tempDir, "microbatConfig.json").exists());
    }

    @AfterEach
    public void deleteTempDir() throws IOException {
        FileUtils.deleteDirectory(tempDir);
    }
}
