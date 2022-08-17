package jmutation.model;

import microbat.model.value.VarValue;

import java.util.List;

public class TestIO {
    private List<VarValue> inputs;
    private VarValue output;

    private boolean hasPassed;

    public TestIO(List<VarValue> inputs, VarValue output) {
        this.inputs = inputs;
        this.output = output;
    }

    public void setHasPassed(boolean hasPassed) {
        this.hasPassed = hasPassed;
    }

    public List<VarValue> getInputs() {
        return inputs;
    }

    public VarValue getOutput() {
        return output;
    }

    public boolean hasPassed() {
        return hasPassed;
    }
}
