package jmutation.experiment;

import jmutation.execution.Defects4jExecutor;
import jmutation.model.mutation.MutationRange;
import jmutation.model.project.Defects4jProject;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Defects4jExperiment {
    private static final String[] PROJECT_NAMES = {"Chart"};
    private static final int[] BUG_IDS = {26};

    public void run() {
        String path = Path.of("C:", "Users", "bchenghi", "Desktop", "mutation-experiment").toString();
        for (int i = 0; i < PROJECT_NAMES.length; i++) {
            String projName = PROJECT_NAMES[i];
            int totalBugs = BUG_IDS[i];
            for (int j = 1; j <= totalBugs; j++) {
                // Checkout each project
                Defects4jProject.checkoutCommand(projName, j + "f", path);
                // Compile
                Defects4jProject project = new Defects4jProject(projName, new File(path), null, null, null, null, null);
                Defects4jExecutor executor = new Defects4jExecutor(project);
                executor.compile();
                // Get changed class names -> mutation ranges
                List<String> classes = executor.getClassDiffs();
                List<MutationRange> mutationRanges = convertClassesToMutationRanges(classes);

            }
        }
    }

    public List<MutationRange> convertClassesToMutationRanges(List<String> classes) {
        List<MutationRange> result = new ArrayList<>();
        for (String clazz : classes) {
            result.add(new MutationRange(clazz, 0, 0));
        }
        return result;

    }

}
