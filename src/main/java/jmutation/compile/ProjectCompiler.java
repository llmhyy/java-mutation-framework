package jmutation.compile;

import jmutation.compile.execution.Executor;
import jmutation.model.Project;

/**
 * 
 * Given an arbitrary project (maven or gradle), we shall find a way to compile it.
 * 
 * @author Yun Lin
 *
 */
public class ProjectCompiler extends Executor {
    private final Project project;

    public ProjectCompiler(Project project) {
        super(project.getRoot());
        this.project = project;
    }

}
