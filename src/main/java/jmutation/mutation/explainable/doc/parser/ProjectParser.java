package jmutation.mutation.explainable.doc.parser;

import jmutation.mutation.explainable.doc.model.JavaComment;
import jmutation.mutation.explainable.doc.model.JavaFileComment;
import jmutation.mutation.explainable.doc.model.Project;
import jmutation.mutation.explainable.doc.parser.handler.ProjectParserFilter;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Parses a project to obtain its documentation
 */
public class ProjectParser {
    private final Project project;
    private final List<ProjectParserFilter> filters;

    private ProjectParser(Project project) {
        this.project = project;
        filters = new ArrayList<>();
    }

    private void addFilter(ProjectParserFilter filter) {
        filters.add(filter);
    }

    public Iterator<JavaFileComment> parse() throws IOException {
        Iterator<File> javaFiles = project.getJavaFiles();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return javaFiles.hasNext();
            }

            @Override
            public JavaFileComment next() {
                File file = javaFiles.next();
                try {
                    String canonicalPath = file.getCanonicalPath();
                    Path path = Paths.get(canonicalPath);
                    String contents = Files.readString(path);
                    CompilationUnit unit = jmutation.parser.ProjectParser.parseCompilationUnit(contents);
                    List<Comment> comments = unit.getCommentList();
                    JavaFileComment result = new JavaFileComment();
                    for (Comment comment : comments) {
                        result.addComment(convert(comment, unit, canonicalPath, contents));
                    }
                    return result;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            private JavaComment convert(Comment comment, CompilationUnit unit, String path, String source) {
                int startPos = comment.getStartPosition();
                int endPos = startPos + comment.getLength();
                int startLine = unit.getLineNumber(startPos);
                int endLine = unit.getLineNumber(endPos);
                return new JavaComment(source.substring(startPos, endPos), startLine, endLine, path);
            }
        };
    }

    public static class ProjectParserBuilder {
        private final Project project;
        private final List<ProjectParserFilter> filters;

        public ProjectParserBuilder(Project project) {
            this.project = project;
            filters = new ArrayList<>();
        }

        public void setFilter(ProjectParserFilter filter) {
            filters.add(filter);
        }

        public ProjectParser build() {
            ProjectParser result = new ProjectParser(project);
            for (ProjectParserFilter filter : filters) {
                result.addFilter(filter);
            }
            return result;
        }
    }
}
