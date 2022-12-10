package jmutation.mutation.explainable;

import jmutation.execution.Coverage;
import jmutation.execution.ProjectExecutor;
import jmutation.model.mutation.MutationRange;
import jmutation.model.project.Project;
import jmutation.model.project.ProjectConfig;
import jmutation.mutation.MutationCommand;
import jmutation.mutation.Mutator;
import jmutation.mutation.explainable.doc.DocumentBasedMutationCommand;
import jmutation.mutation.explainable.doc.code.CodeChunkGetter;
import jmutation.mutation.explainable.doc.code.CornerCaseCommentFocus;
import jmutation.mutation.explainable.doc.code.matcher.SemanticMatcher;
import jmutation.mutation.explainable.doc.model.CodeChunk;
import jmutation.mutation.explainable.doc.model.JavaComment;
import jmutation.mutation.explainable.doc.model.JavaFileComment;
import jmutation.mutation.explainable.doc.parser.ProjectCommentParser;
import jmutation.mutation.explainable.doc.parser.ProjectCommentParser.ProjectParserBuilder;
import jmutation.mutation.explainable.doc.parser.handler.CoverageFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExplainableMutator extends Mutator {
    @Override
    public Project mutate(Coverage coverage, Project project) {
        // Only mutate coverage
        // go through the coverage
        // Only parse from files in coverage
        Project clonedProject = project.cloneToOtherPath();
        try {
            jmutation.mutation.explainable.doc.model.Project projectForParser =
                    new jmutation.mutation.explainable.doc.model.Project(clonedProject.getRoot().getCanonicalPath());
            CoverageFilter coverageFilter = new CoverageFilter(coverage);
            ProjectCommentParser projectCommentParser = new ProjectParserBuilder(projectForParser).
                    addFilter(coverageFilter).
                    build();
            Iterator<JavaFileComment> commentsInFiles = projectCommentParser.parse();
            CornerCaseCommentFocus focus = new CornerCaseCommentFocus();
            CodeChunkGetter codeChunkGetter = new CodeChunkGetter(focus, new SemanticMatcher());
            while (commentsInFiles.hasNext()) {
                JavaFileComment fileComments = commentsInFiles.next();
                List<JavaComment> comments = fileComments.getComments();
                for (JavaComment comment : comments) {
                    List<CodeChunk> codeChunks = codeChunkGetter.get(comment, comment.getFilePath());
                    for (CodeChunk codeChunk : codeChunks) {
                        MutationCommand command = parseCodeAndCommentIntoMutation(codeChunk, comment);
                        if (!command.canExecute()) {
                            continue;
                        }
                        command.executeMutation();
                        if (!compileProject(clonedProject)) {
                            continue;
                        }
                        mutationHistory.add(command);
                        return clonedProject;
                    }
                }
            }
            return clonedProject;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return project;
    }

    private MutationCommand parseCodeAndCommentIntoMutation(CodeChunk codeChunk, JavaComment comment) {
        // Perhaps can use comment type to determine mutation type
        return new DocumentBasedMutationCommand(codeChunk.getNode());
    }

    @Override
    public List<MutationCommand> analyse(List<MutationRange> ranges, Project project) {
        // TODO: Implement this
        return new ArrayList<>();
    }

    public boolean compileProject(Project project) {
        ProjectConfig config = new ProjectConfig(project);
        ProjectExecutor projectExecutor = new ProjectExecutor(null, config);
        String output = projectExecutor.compile();
        return output.contains("BUILD SUCCESS");
    }
}
