package jmutation.model;

import org.eclipse.core.internal.preferences.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MicrobatConfig {
    public static final String OPT_CLASS_PATH = "class_path";
    public static final String OPT_WORKING_DIR = "working_dir";
    public static final String OPT_LOG = "log";

    public static final String OPT_PRECHECK = "precheck";
    public static final String OPT_ENTRY_POINT = "entry_point";
    public static final String OPT_LAUNCH_CLASS = "launch_class";
    public static final String OPT_JAVA_HOME = "java_home";
    public static final String OPT_DUMP_FILE = "dump_file_path";
    public static final String OPT_TCP_PORT = "tcp_port";
    public static final String OPT_INCLUDES = "includes";
    public static final String OPT_EXCLUDES = "excludes";
    public static final String OPT_VARIABLE_LAYER = "varLayer";
    public static final String OPT_STEP_LIMIT = "stepLimit";
    public static final String OPT_EXPECTED_STEP = "expectedSteps";
    public static final String OPT_INCLUDES_FILE = "includes_file";
    public static final String OPT_EXCLUDES_FILE = "excludes_file";
    public static final String OPT_OVER_LONG_METHODS = "overlong_methods";
    public static final String OPT_REQUIRE_METHOD_SPLITTING = "require_method_split";
    public static final String OPT_AVOID_TO_STRING_OF_PROXY_OBJ = "avoid_proxy_tostring";
    public static final String OPT_CODE_RANGE = "code_range";
    public static final String OPT_TRACE_RECORDER = "trace_recorder";
    public static final String OPT_RUN_ID = "run_id";

    private static final Set validKeys = Set.of(OPT_CLASS_PATH, OPT_WORKING_DIR, OPT_LOG, OPT_PRECHECK, OPT_ENTRY_POINT,
            OPT_INCLUDES, OPT_EXCLUDES, OPT_VARIABLE_LAYER, OPT_STEP_LIMIT, OPT_EXPECTED_STEP, OPT_INCLUDES_FILE,
            OPT_EXCLUDES_FILE, OPT_OVER_LONG_METHODS, OPT_REQUIRE_METHOD_SPLITTING, OPT_AVOID_TO_STRING_OF_PROXY_OBJ,
            OPT_LAUNCH_CLASS, OPT_JAVA_HOME, OPT_DUMP_FILE, OPT_TCP_PORT, OPT_CODE_RANGE, OPT_TRACE_RECORDER,
            OPT_RUN_ID);

    private final Map<String, List<String>> argMap;

    private MicrobatConfig(Map<String, List<String>> argMap) {
        this.argMap = argMap;
    }

    public static MicrobatConfig parse(String pathToConfigFile) {
        // TODO: parse config from txt file
        return new MicrobatConfig(null);
    }

    public static MicrobatConfig defaultConfig() {
        // TODO: add minimal working config
        Map<String, List<String>> argMap = Map.of();
        return new MicrobatConfig(argMap);
    }
}
