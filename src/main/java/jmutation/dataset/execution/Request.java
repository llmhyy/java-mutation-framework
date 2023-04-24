package jmutation.dataset.execution;

public class Request {
    private final boolean hasPassed;

    public Request(boolean hasPassed) {
        this.hasPassed = hasPassed;
    }

    public boolean hasPassed() {
        return hasPassed;
    }
}
