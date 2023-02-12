package jmutation.model;

import jmutation.model.project.MavenProject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MavenProjectTest {
    @Test
    void getExternalLibs_MavenProjectInitialisedWithExternalLibs_GetsExternalLibs() {
        List<File> expectedResult = new ArrayList<>();
        expectedResult.add(new File("externalLib.jar"));
        expectedResult.add(new File("externalLib1.jar"));
        MavenProject mavenProject = new MavenProject(null, null, null, null,
                null, null, null, expectedResult);
        assertEquals(expectedResult, mavenProject.getExternalLibs());
    }
}
