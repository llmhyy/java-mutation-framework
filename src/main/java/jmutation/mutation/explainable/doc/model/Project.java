package jmutation.mutation.explainable.doc.model;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Iterator;

public class Project {
    private final String projectRoot;

    public Project(String projectRoot) {
        this.projectRoot = projectRoot;
    }

    public Iterator<File> getJavaFiles() {
        return FileUtils.iterateFiles(new File(projectRoot), new String[]{"java"}, true);
    }
}
