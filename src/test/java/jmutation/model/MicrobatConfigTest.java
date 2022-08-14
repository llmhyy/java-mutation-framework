package jmutation.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MicrobatConfigTest {
    @Test
    void parse_validConfigFile_correctMicrobatConfig() {
        String validConfigFilePath = "src/test/files/jmutation/model/validMicrobatConfigTest.json";
        String projectPath = "./project";
        MicrobatConfig actualConfig = MicrobatConfig.parse(validConfigFilePath, projectPath);
        Map<String, List<String>> expectedConfigMap = new HashMap<>();
        expectedConfigMap.put("java_home", Arrays.asList("C:/java"));
        expectedConfigMap.put("entry_point", Arrays.asList("C:/path/to/entry/point/0", "C:/path/to/entry/point/1"));

        // defaults
        expectedConfigMap.put("precheck", Arrays.asList("false"));
        expectedConfigMap.put("run_id", Arrays.asList("1763794d-c0c2-4704-a483-20725cb39fd3"));
        expectedConfigMap.put("log", Arrays.asList("printProgress", "error"));
        expectedConfigMap.put("varLayer", Arrays.asList("5"));
        expectedConfigMap.put("working_dir", Arrays.asList(projectPath));
        expectedConfigMap.put("trace_recorder", Arrays.asList("FILE"));
        expectedConfigMap.put("dump_file_path", Arrays.asList(System.getProperty("java.io.tmpdir") + "dumpFile.exec"));
        expectedConfigMap.put("stepLimit", Arrays.asList("400000"));
        MicrobatConfig expectedConfig = new MicrobatConfig(expectedConfigMap);
        assertEquals(expectedConfig, actualConfig);
    }
}
