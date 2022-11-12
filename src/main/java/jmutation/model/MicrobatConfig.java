package jmutation.model;

import jmutation.utils.JSONWrapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.valueOf;

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

    private static final Set<String> validKeys = Set.of(OPT_CLASS_PATH, OPT_WORKING_DIR, OPT_LOG, OPT_PRECHECK, OPT_ENTRY_POINT,
            OPT_INCLUDES, OPT_EXCLUDES, OPT_VARIABLE_LAYER, OPT_STEP_LIMIT, OPT_EXPECTED_STEP, OPT_INCLUDES_FILE,
            OPT_EXCLUDES_FILE, OPT_OVER_LONG_METHODS, OPT_REQUIRE_METHOD_SPLITTING, OPT_AVOID_TO_STRING_OF_PROXY_OBJ,
            OPT_LAUNCH_CLASS, OPT_JAVA_HOME, OPT_DUMP_FILE, OPT_TCP_PORT, OPT_CODE_RANGE, OPT_TRACE_RECORDER,
            OPT_RUN_ID);
    private static final String AGENT_OPTION_SEPARATOR = "=";
    private static final String AGENT_PARAMS_SEPARATOR = ",";
    private static final String AGENT_PARAMS_MULTI_VALUE_SEPARATOR = File.pathSeparator;
    private final Map<String, List<String>> argMap;

    MicrobatConfig(Map<String, List<String>> argMap) {
        this.argMap = argMap;
    }

    public static MicrobatConfig parse(String pathToConfigFile, String projectPath) {
        JSONObject jsonConfig = JSONWrapper.getJSONObjectFromFile(pathToConfigFile);
        Iterator<String> jsonConfigIterator = jsonConfig.keys();

        MicrobatConfig newMicrobatConfig = defaultConfig(projectPath);

        while (jsonConfigIterator.hasNext()) {
            String key = jsonConfigIterator.next();
            JSONArray valuesArray = jsonConfig.getJSONArray(key);
            List<String> valuesList = new ArrayList<>();
            for (int i = 0; i < valuesArray.length(); i++) {
                String value = valuesArray.getString(i);
                valuesList.add(value);
            }
            newMicrobatConfig = newMicrobatConfig.updateEntry(key, valuesList);
        }

        return newMicrobatConfig;
    }

    public static MicrobatConfig defaultConfig(String projectPath) {
        MicrobatConfig microbatConfig = MicrobatConfig.defaultConfig();
        return microbatConfig.setWorkingDir(projectPath);
    }

    public static MicrobatConfig defaultConfig() {
        Map<String, List<String>> argMap = new HashMap<>();
        argMap.put(OPT_DUMP_FILE, List.of(System.getProperty("java.io.tmpdir") + "dumpFile.exec"));
        if (System.getenv("JAVA_HOME") != null) {
            argMap.put(OPT_JAVA_HOME, List.of(System.getenv("JAVA_HOME")));
        }
        argMap.put(OPT_STEP_LIMIT, List.of("400000"));
        argMap.put(OPT_TRACE_RECORDER, List.of("FILE"));
        argMap.put(OPT_VARIABLE_LAYER, List.of("10"));
        argMap.put(OPT_LOG, List.of("printProgress", "error"));
        argMap.put(OPT_PRECHECK, List.of("false"));
        argMap.put(OPT_RUN_ID, List.of("1763794d-c0c2-4704-a483-20725cb39fd3"));
        return new MicrobatConfig(argMap);
    }

    private MicrobatConfig updateEntry(String key, List<String> values) {
        if (!validKeys.contains(key)) {
            throw new RuntimeException("Updating invalid agent parameter " + key);
        }
        Map<String, List<String>> newArgMap = new HashMap<>(argMap);
        newArgMap.put(key, values);
        return new MicrobatConfig(newArgMap);
    }

    public MicrobatConfig setClassPaths(List<String> classPaths) {
        return updateEntry(OPT_CLASS_PATH, classPaths);
    }

    public MicrobatConfig setTraceRecorder(String traceRecorder) {
        return updateEntry(OPT_TRACE_RECORDER, Arrays.asList(traceRecorder));
    }

    public MicrobatConfig setLaunchClass(String launchClass) {
        return updateEntry(OPT_LAUNCH_CLASS, Arrays.asList(launchClass));
    }

    public MicrobatConfig setIncludes(List<String> includedClasses) {
        return updateEntry(OPT_INCLUDES, includedClasses);
    }

    public MicrobatConfig setExpectedSteps(int expectedSteps) {
        return updateEntry(OPT_EXPECTED_STEP, Arrays.asList(String.valueOf(expectedSteps)));
    }

    public MicrobatConfig setVariableLayer(int variableLayer) {
        return updateEntry(OPT_VARIABLE_LAYER, Arrays.asList(String.valueOf(variableLayer)));
    }

    public String getDumpFilePath() {
        return argMap.get(OPT_DUMP_FILE).get(0);
    }

    public MicrobatConfig setDumpFilePath(String dumpFilePath) {
        return updateEntry(OPT_DUMP_FILE, Arrays.asList(dumpFilePath));
    }

    public boolean getPrecheck() {
        return Boolean.parseBoolean(argMap.get(OPT_PRECHECK).get(0));
    }

    public MicrobatConfig setPrecheck(boolean usePrecheck) {
        return updateEntry(OPT_PRECHECK, Arrays.asList(valueOf(usePrecheck)));
    }

    public String getJavaHome() {
        return argMap.get(OPT_JAVA_HOME).get(0);
    }

    public MicrobatConfig setJavaHome(String javaHome) {
        return updateEntry(OPT_JAVA_HOME, Arrays.asList(javaHome));
    }

    public String getClassPathStr() {
        List<String> classPaths;
        if (argMap.containsKey(OPT_CLASS_PATH)) {
            classPaths = argMap.get(OPT_CLASS_PATH);
        } else {
            classPaths = new ArrayList<>();
        }
        return String.join(AGENT_PARAMS_MULTI_VALUE_SEPARATOR, classPaths);
    }

    public String getWorkingDir() {
        if (argMap.containsKey(OPT_WORKING_DIR)) {
            return argMap.get(OPT_WORKING_DIR).get(0);
        }
        return "";
    }

    public MicrobatConfig setWorkingDir(String workingDir) {
        return updateEntry(OPT_WORKING_DIR, Arrays.asList(workingDir));
    }

    public int getStepLimit() {
        return Integer.parseInt(argMap.get(OPT_STEP_LIMIT).get(0));
    }

    public MicrobatConfig setStepLimit(int stepLimit) {
        return updateEntry(OPT_STEP_LIMIT, Arrays.asList(String.valueOf(stepLimit)));
    }

    public boolean getRequireMethodSplitting() {
        return Boolean.parseBoolean(argMap.get(OPT_REQUIRE_METHOD_SPLITTING).get(0));
    }

    public MicrobatConfig setRequireMethodSplitting(boolean requireMethodSplit) {
        return updateEntry(OPT_REQUIRE_METHOD_SPLITTING, Arrays.asList(String.valueOf(requireMethodSplit)));
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof MicrobatConfig)) {
            return false;
        }

        MicrobatConfig otherMicrobatConfig = (MicrobatConfig) that;
        Map<String, List<String>> otherArgMap = otherMicrobatConfig.argMap;
        if (argMap.size() != otherArgMap.size()) {
            return false;
        }

        for (Map.Entry<String, List<String>> keyValuePair : argMap.entrySet()) {
            String key = keyValuePair.getKey();
            if (!otherArgMap.containsKey(key)) {
                return false;
            }
            List<String> values = keyValuePair.getValue();
            List<String> otherValues = otherArgMap.get(key);
            if (!values.equals(otherValues)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        List<String> options = new ArrayList<>();
        for (Map.Entry<String, List<String>> keyValuePair : argMap.entrySet()) {
            StringBuilder optionBuilder = new StringBuilder();
            String key = keyValuePair.getKey();
            optionBuilder.append(key);
            List<String> values = keyValuePair.getValue();
            optionBuilder.append(AGENT_OPTION_SEPARATOR);
            String combinedValues = String.join(AGENT_PARAMS_MULTI_VALUE_SEPARATOR, values);

            if (key.equals("java_home")) {
                combinedValues = "\"" + combinedValues + "\"";
            }

            optionBuilder.append(combinedValues);
            options.add(optionBuilder.toString());
        }
        return String.join(AGENT_PARAMS_SEPARATOR, options);
    }
}
