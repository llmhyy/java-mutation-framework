package jmutation.mutation.explainable.doc.knowledge;

import java.util.ArrayList;
import java.util.List;

public class APIMethodKnowledge {
    private final List<String> functionalities;
    private final List<String> directives;
    private final List<Parameter> parameters;

    public APIMethodKnowledge(List<String> functionalities, List<String> directives, List<Parameter> parameters) {
        this.functionalities = functionalities;
        this.directives = directives;
        this.parameters = parameters;
    }

    public List<String> getFunctionalities() {
        return functionalities;
    }

    public List<String> getDirectives() {
        return directives;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public static class APIMethodKnowledgeBuilder {
        private List<String> functionalities = new ArrayList<>();
        private List<String> directives = new ArrayList<>();
        private List<Parameter> parameters = new ArrayList<>();

        public void setFunctionalities(List<String> functionalities) {
            this.functionalities = functionalities;
        }

        public void setDirectives(List<String> directives) {
            this.directives = directives;
        }

        public void setParameters(List<Parameter> parameters) {
            this.parameters = parameters;
        }

        public APIMethodKnowledge build() {
            return new APIMethodKnowledge(functionalities, directives, parameters);
        }
    }
}
