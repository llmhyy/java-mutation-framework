package jmutation.parser;

import jmutation.model.MavenProject;
import jmutation.model.Project;
import jmutation.model.ProjectConfig;
import jmutation.model.TestCase;
import jmutation.model.ast.JdtMethodRetriever;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Given a maven or gradle project path, we parse it into a project
 *
 * @author Yun Lin
 */
public class ProjectParser {
    private final ProjectConfig projectConfig;
    private Project project;
    private final File root;

    public ProjectParser(ProjectConfig projectConfig) {
        this.projectConfig = projectConfig;
        root = new File(projectConfig.getProjectPath());
    }

    public Project parse() {
        if (project == null) {
            // traverse project differently depending on project type ?
            // assume Maven project by default for now
            this.project = new MavenProject(root, walk(root));
        }
        return this.project;
    }

    private List<TestCase> walk(File start) {
        File[] list = start.listFiles();
        List<TestCase> testCases = new ArrayList<>();

        for (File f : list) {
            if (f.isDirectory()) {
                testCases.addAll(walk(f));
            } else {
                // 1. use detection regminer detection logic
                try {
                    String fileContent = Files.readString(f.toPath());
                    if (isTestSuite(fileContent)) {
                        // get walk code and retrieve all methods
                        // 2. use regminer jdt ast Visitor code from regminer to identify testcases
                        testCases.addAll(getAllMethod(fileContent));
                    }
                } catch (IOException e) {
                    System.out.print("Unable to open file at ");
                    System.out.println(f.getAbsolutePath());
                }
            }
        }
        return testCases;
    }

    private boolean isTestSuite(String code) {
        return code.contains("junit") || code.contains("@Test");
    }

    private static CompilationUnit parseCompliationUnit(String fileContent) {

        ASTParser parser = ASTParser.newParser(AST.getJLSLatest()); // handles JDK 1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6
        parser.setSource(fileContent.toCharArray());
        // In order to parse 1.6 code, some compiler options need to be set to 1.6
        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
        parser.setCompilerOptions(options);

        CompilationUnit result = (CompilationUnit) parser.createAST(null);
        return result;
    }

    /**
     * Returns a list of methods from the String representation of a file.
     * @param codeContent file content as String
     * @return List of methods
     */
    public static List<TestCase> getAllMethod(String codeContent) {
        // code taken from regminer
        List<TestCase> methods = new ArrayList<>();
        JdtMethodRetriever retriever = new JdtMethodRetriever();
        CompilationUnit unit = parseCompliationUnit(codeContent);
        unit.accept(retriever);
        List<MethodDeclaration> methodNodes = retriever.getMethods();
        String className = retriever.getClassName();
        for (MethodDeclaration node : methodNodes) {
            if (!(node.getParent().getParent() instanceof CompilationUnit) ){
                continue;
            }
            if (isIgnoredMethod(node) || !isTestMethod(node)) {
                // skip nodes with @Ignore annotation
                // skip nodes without @Test annotation
                continue;
            }

            String simpleName = node.getName().toString();
            StringJoiner sj = new StringJoiner(",", simpleName + "(", ")");
            node.parameters().stream().forEach(param -> sj.add(param.toString()));
            String signature = sj.toString();

            int startLine = unit.getLineNumber(node.getStartPosition()) - 1;
            int endLine = unit.getLineNumber(node.getStartPosition() + node.getLength()) - 1;
            methods.add(new TestCase(signature, startLine, endLine, simpleName, className, node));
        }
        return methods;
    }

    private static boolean isIgnoredMethod(MethodDeclaration node) {
        return matchAnnotation(node, "@Ignore");
    }

    private static boolean isTestMethod(MethodDeclaration node) {
        return matchAnnotation(node, "@Test");
    }

    private static boolean matchAnnotation(MethodDeclaration node, String annotation) {
        return node.modifiers().stream().filter(mod -> mod instanceof Annotation).anyMatch(an -> an.toString().equals(annotation));
    }
}
