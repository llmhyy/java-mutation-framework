package jmutation.execution;

import jmutation.model.MicrobatConfig;
import jmutation.model.project.ProjectConfig;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectExecutorTest {

    @Test
    void findJars() {
        String projectPath = "./sample/spoon";
        String dropInsDir = "./lib";
        ProjectConfig config = new ProjectConfig(projectPath, dropInsDir);
        ProjectExecutor pe = new ProjectExecutor(MicrobatConfig.defaultConfig(projectPath), config);
        pe.packageProj();
        List<String> jarNames = pe.findJars().stream().map(file -> file.getName()).collect(Collectors.toList());
        Iterator<File> expectedJarsIterator = FileUtils.iterateFiles(new File(projectPath + "/target"), new String[]{"jar"}, true);
        List<String> expectedJars = new ArrayList<>();
        while (expectedJarsIterator.hasNext()) {
            File jar = expectedJarsIterator.next();
            expectedJars.add(jar.getName());
        }
        assertEquals(expectedJars, jarNames);
    }
}