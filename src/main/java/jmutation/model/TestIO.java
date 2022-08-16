package jmutation.model;

import microbat.model.value.VarValue;

import java.util.List;

public class TestIO {
    private List<VarValue> inputs;
    private VarValue output;
    public TestIO(List<VarValue> inputs, VarValue output) {
        this.inputs = inputs;
        this.output = output;
    }

    public List<VarValue> getInputs() {
        return inputs;
    }

    public VarValue getOutput() {
        return output;
    }
}
