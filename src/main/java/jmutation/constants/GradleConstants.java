package jmutation.constants;

import java.io.File;

public class GradleConstants {
        public static final String BUILD_FOLDER = "build";
        public static final String RELATIVE_COMPILATION_FOLDER_FROM_BUILD = "classes" + File.separator + "java";
        public static final String COMPILATION_FOLDER = BUILD_FOLDER + File.separator + RELATIVE_COMPILATION_FOLDER_FROM_BUILD;
        public static final String TEST_CLASS_FOLDER = "test";
        public static final String COMPILED_CLASS_FOLDER = "main";
        public static final String SRC_FOLDER = "src" + File.separator + "main" + File.separator + "java";
        public static final String TEST_FOLDER = "src" + File.separator + "test" + File.separator + "java";
        public static final String GRADLE_CMD = "gradle";
}
