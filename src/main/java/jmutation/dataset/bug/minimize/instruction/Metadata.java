package jmutation.dataset.bug.minimize.instruction;


import java.util.List;

public class Metadata {
    public static final String PATH_TO_FIXED_KEY = "fixedPath";
    public static final String PATH_TO_BUGGY_KEY = "buggyPath";
    public static final String PATH_TO_METADATA_KEY = "metadataPath";
    public static final String INSTRUCTIONS_KEY = "instructions";
    private final List<Instruction> instructionList;
    private final String pathToFixed;
    private final String pathToBuggy;
    private final String pathToMetadata;

    public Metadata(List<Instruction> instructionList, String pathToFixed, String pathToBuggy, String pathToMetadata) {
        this.instructionList = instructionList;
        this.pathToFixed = pathToFixed;
        this.pathToBuggy = pathToBuggy;
        this.pathToMetadata = pathToMetadata;
    }

    public List<Instruction> instructionList() {
        return instructionList;
    }

    public String pathToFixed() {
        return pathToFixed;
    }

    public String pathToBuggy() {
        return pathToBuggy;
    }

    public String pathToMetaData() {
        return pathToMetadata;
    }
}
