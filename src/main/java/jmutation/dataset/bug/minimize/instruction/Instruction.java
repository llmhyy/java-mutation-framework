package jmutation.dataset.bug.minimize.instruction;

import org.json.JSONObject;

public class Instruction {
    public static final String PATH_IN_METADATA_KEY = "pathInMetadata";
    public static final String PATH_IN_TARGET_KEY = "pathInTarget";
    public static final String INSTRUCTION_TYPE_KEY = "instructionType";
    private final InstructionType instructionType;
    private final String pathInMetadata;
    private final String pathInTarget;

    public Instruction(InstructionType instructionType, String pathInMetadata, String pathInTarget) {
        this.instructionType = instructionType;
        this.pathInMetadata = pathInMetadata;
        this.pathInTarget = pathInTarget;
    }

    public InstructionType instructionType() {
        return instructionType;
    }

    public String pathInMetadata() {
        return pathInMetadata;
    }

    public String pathInTarget() {
        return pathInTarget;
    }

    public JSONObject getValue() {
        JSONObject result = new JSONObject();
        result.put(INSTRUCTION_TYPE_KEY, instructionType);
        result.put(PATH_IN_METADATA_KEY, pathInMetadata);
        result.put(PATH_IN_TARGET_KEY, pathInTarget);
        return result;
    }

    public enum InstructionType {
        ADD, DELETE
    }
}
