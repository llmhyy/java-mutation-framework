package jmutation.utils;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

public class JSONWrapper {
    public static JSONObject getJSONObjectFromFile(String filePath) {
        File configFile = new File(filePath);
        if (!configFile.exists()) {
            throw new RuntimeException("JSON file " + configFile.getAbsolutePath() + " not found!");
        }
        return getJSONObjectFromFile(configFile);
    }

    public static JSONObject getJSONObjectFromFile(File file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath(),
                    Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException("Could not Files.readAllLines from JSON file at " + file.getAbsolutePath());
        }

        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line);
        }
        String jsonStringConfig = sb.toString();
        return new JSONObject(jsonStringConfig);
    }
}
