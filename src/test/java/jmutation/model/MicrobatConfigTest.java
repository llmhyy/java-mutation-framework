package jmutation.model;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MicrobatConfigTest {
    @Test
    void parse_validConfigFile_correctMicrobatConfig() {
        String validConfigFilePath = "src/test/files/jmutation/model/validMicrobatConfigTest.json";
        MicrobatConfig actualConfig = MicrobatConfig.parse(validConfigFilePath);
        Map<String, List<String>> expectedConfigMap = new HashMap<>();
        expectedConfigMap.put("java_home", Arrays.asList("C:/java"));
        expectedConfigMap.put("entry_point", Arrays.asList("C:/path/to/entry/point/0", "C:/path/to/entry/point/1"));
        MicrobatConfig expectedConfig = new MicrobatConfig(expectedConfigMap);
        assertEquals(expectedConfig, actualConfig);
    }
}
