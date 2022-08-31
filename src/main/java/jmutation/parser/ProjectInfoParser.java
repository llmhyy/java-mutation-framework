package jmutation.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

abstract class ProjectInfoParser {
    private final File root;
    public ProjectInfoParser(File root) {
        this.root = root;
    }
    String getProjectName() {
        return root.getName();
    }
    abstract String getSrcFolderPath();
    abstract String getTestFolderPath();
    abstract String getCompiledSrcFolderPath();
    abstract String getCompiledTestFolderPath();

    protected File getRoot() {
        return root;
    }

}
