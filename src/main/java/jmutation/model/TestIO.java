package jmutation.model;

import microbat.model.value.VarValue;

import java.util.List;

public class TestIO {
    private List<VarValue> inputs;
    private List<VarValue> outputs;
    public TestIO(List<VarValue> inputs, List<VarValue> outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public List<VarValue> getInputs() {
        return inputs;
    }

    public List<VarValue> getOutputs() {
        return outputs;
    }
}
