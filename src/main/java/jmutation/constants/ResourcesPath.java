package jmutation.constants;

import java.io.File;

public class ResourcesPath {
    final public static String DEFAULT_RESOURCES_PATH = String.join(File.separator,
            System.getenv("USERPROFILE"), "lib", "resources", "java-mutation-framework");
    final public static String DEFAULT_DROP_INS_DIR = "lib";
    final public static String DEFAULT_SEMSEED_DIR = "semantic";
    final public static String DEFAULT_SEMSEED_MODEL = "token-embeddings-FT.bin";
    final public static String DEFAULT_SEMSEED_PATTERNS = "bug-fix-patterns.json";
}
