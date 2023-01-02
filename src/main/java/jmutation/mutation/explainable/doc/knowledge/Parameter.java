package jmutation.mutation.explainable.doc.knowledge;

public class Parameter {
    private final String name;
    private final String description;

    public Parameter(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name + " - " + description;
    }
}
