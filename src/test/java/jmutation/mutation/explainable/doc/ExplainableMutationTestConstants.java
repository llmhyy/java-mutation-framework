package jmutation.mutation.explainable.doc;

import java.io.File;

public class ExplainableMutationTestConstants {
    public static final String PARSER_SAMPLE_PROJECT_PATH = String.join(File.separator, "src", "test", "files",
            "jmutation", "mutation", "explainable", "doc", "parser", "sample-project");

    public static final String CODE_SAMPLE_JAVA_FILE_PATH = String.join(File.separator, "src", "test", "files", "jmutation", "mutation", "explainable", "doc", "code", "Sample.java");
    public static final String EXPLAINABLE_FILE_PATH = String.join(File.separator, "src", "test", "files", "jmutation", "mutation", "explainable", "doc");
    public static final String EXPLAINABLE_SAMPLE_PROJECT_PATH = String.join(File.separator, EXPLAINABLE_FILE_PATH, "sample-maven-project");
    public static final String EXPLAINABLE_SAMPLE_PROJECT_PATH_TO_MUTATE = String.join(File.separator, EXPLAINABLE_FILE_PATH, "sample-maven-project-to-mutate");
}
