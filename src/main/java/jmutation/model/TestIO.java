package jmutation.model;

import microbat.model.value.VarValue;

import java.util.List;

public class TestIO {
    private List<VarValue> readVariables;
    private List<VarValue> writtenVariables;
    public TestIO(List<VarValue> readVariables, List<VarValue> writtenVariables) {
        this.readVariables = readVariables;
        this.writtenVariables = writtenVariables;
    }

    public List<VarValue> getReadVariables() {
        return readVariables;
    }

    public List<VarValue> getWrittenVariables() {
        return writtenVariables;
    }
}
