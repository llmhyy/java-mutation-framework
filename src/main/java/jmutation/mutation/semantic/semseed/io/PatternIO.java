package jmutation.mutation.semantic.semseed.io;

import jmutation.mutation.semantic.semseed.io.handler.IOHandler;
import jmutation.mutation.semantic.semseed.model.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class PatternIO {
    public static final String KEY_BUGGY = "buggy";
    public static final String KEY_FIX = "fix";
    public static final String KEY_FIX_ACTUAL = "fix_actual";
    public static final String KEY_BUGGY_ACTUAL = "buggy_actual";
    IOHandler ioHandler;

    public PatternIO(IOHandler ioHandler) {
        this.ioHandler = ioHandler;
    }

    public boolean write(Pattern pattern) {
        return false;
    }

    public List<Pattern> readAll() {
        List<Pattern> result = new ArrayList<>();
        String patternFileContents;
        try {
            patternFileContents = ioHandler.readAll();
        } catch (IOException e) {
            return result;
        }
        JSONArray jsonConfig = new JSONArray(patternFileContents);
        Iterator<Object> iterator = jsonConfig.iterator();
        while (iterator.hasNext()) {
            JSONObject patternJsonObj = (JSONObject) iterator.next();
            JSONArray buggy = patternJsonObj.getJSONArray(KEY_BUGGY);
            List<String> buggyLs = convertObjLsToStrLs(buggy.toList());
            JSONArray fix = patternJsonObj.getJSONArray(KEY_FIX);
            List<String> fixLs = convertObjLsToStrLs(fix.toList());
            JSONArray buggyActual = patternJsonObj.getJSONArray(KEY_BUGGY_ACTUAL);
            List<String> buggyActLs = convertObjLsToStrLs(buggyActual.toList());
            JSONArray fixActual = patternJsonObj.getJSONArray(KEY_FIX_ACTUAL);
            List<String> fixActLs = convertObjLsToStrLs(fixActual.toList());
            result.add(new Pattern(buggyLs, fixLs, buggyActLs, fixActLs));
        }
        return result;
    }

    private List<String> convertObjLsToStrLs(List<Object> objectList) {
        return objectList.stream()
                .map(object -> (String) object)
                .collect(Collectors.toList());
    }
}
