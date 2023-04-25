package jmutation.dataset.bug.minimize;

import jmutation.dataset.bug.minimize.instruction.Instruction;
import jmutation.dataset.bug.minimize.instruction.Instruction.InstructionType;
import jmutation.dataset.bug.minimize.instruction.Metadata;
import jmutation.utils.JSONWrapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MetadataParser {
    public static Metadata parse(String instructionsFile) {
        List<Instruction> instructionList = new ArrayList<>();
        JSONObject contents = JSONWrapper.getJSONObjectFromFile(instructionsFile);
        JSONArray instructionsArr = contents.getJSONArray(Metadata.INSTRUCTIONS_KEY);

        for (int i = 0; i < instructionsArr.length(); i++) {
            JSONObject instructionJsonObject = instructionsArr.getJSONObject(i);
            instructionList.add(createInstructionFromJSON(instructionJsonObject));
        }

        String pathToBuggy = contents.getString(Metadata.PATH_TO_BUGGY_KEY);
        String pathToFixed = contents.getString(Metadata.PATH_TO_FIXED_KEY);
        String pathToMetadata = contents.getString(Metadata.PATH_TO_METADATA_KEY);
        return new Metadata(instructionList, pathToFixed, pathToBuggy, pathToMetadata);
    }

    private static Instruction createInstructionFromJSON(JSONObject jsonObject) {
        InstructionType type = InstructionType.valueOf(jsonObject.getString(Instruction.INSTRUCTION_TYPE_KEY));
        return new Instruction(type, jsonObject.getString(Instruction.PATH_IN_METADATA_KEY),
                jsonObject.getString(Instruction.PATH_IN_TARGET_KEY));
    }
}
