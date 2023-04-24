package jmutation.dataset.bug.minimize;

import jmutation.dataset.bug.minimize.instruction.Instruction;
import jmutation.dataset.bug.minimize.instruction.Metadata;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MetadataWriter {
    public static void write(String pathToFile, Metadata metadata) {
        JSONObject jsonObjectResult = new JSONObject();
        jsonObjectResult.put(Metadata.PATH_TO_FIXED_KEY, metadata.pathToFixed());
        jsonObjectResult.put(Metadata.PATH_TO_BUGGY_KEY, metadata.pathToBuggy());
        jsonObjectResult.put(Metadata.PATH_TO_METADATA_KEY, metadata.pathToMetaData());
        List<Instruction> instructionList = metadata.instructionList();
        JSONArray instructionsArray = new JSONArray();
        for (int i = 0; i < instructionList.size(); i++) {
            Instruction instruction = instructionList.get(i);
            instructionsArray.put(instruction.getValue());
        }
        jsonObjectResult.put(Metadata.INSTRUCTIONS_KEY, instructionsArray);
        try (FileWriter metadataFileWriter = new FileWriter(pathToFile)) {
            metadataFileWriter.write(jsonObjectResult.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
