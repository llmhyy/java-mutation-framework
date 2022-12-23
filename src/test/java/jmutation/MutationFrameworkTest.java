package jmutation;

import jmutation.constants.ExternalLibrary;
import jmutation.model.mutation.MutationFrameworkConfig.MutationFrameworkConfigBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MutationFrameworkTest {
    private final String tempDirPath = "./src/test/temp";
    private final File tempDir = new File(tempDirPath);

    @Test
    public void extractResources_provideDirectory_extractsAllIntoDirectory() throws IOException, MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File("./pom.xml"));
        request.setGoals(Collections.singletonList("install -Dmaven.test.skip=true"));
        Invoker invoker = new DefaultInvoker();
        invoker.execute(request);
        MutationFrameworkConfigBuilder configBuilder = new MutationFrameworkConfigBuilder();
        configBuilder.setProjectPath("project path");
        MutationFramework mf = new MutationFramework(configBuilder.build());
        mf.extractResources(tempDirPath);
        for (ExternalLibrary externalLibrary : ExternalLibrary.values()) {
            assertTrue(new File(tempDir, "lib" + File.separator + externalLibrary.getName() + ".jar").exists());
        }
        assertTrue(new File(tempDir, "microbatConfig.json").exists());
        assertTrue(new File(tempDir, "semantic" + File.separator + "bug-fix-patterns.json").exists());
    }

    @AfterEach
    public void deleteTempDir() throws IOException {
        FileUtils.deleteDirectory(tempDir);
    }
}
