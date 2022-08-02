package jmutation.parser;

import jmutation.model.*;
import jmutation.model.ast.JdtMethodRetriever;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * Given a maven or gradle project path, we parse it into a project
 *
 * @author Yun Lin
 */
public class ProjectParser {
    private final File root;
    private Project project;
    private ProjectType projectType = ProjectType.MAVEN;

    public ProjectParser(File root) {
        // TODO: Determine project type (gradle or maven or none from project structure)
        // Currently assume project type is maven
        if (!root.exists()) {
            throw new RuntimeException("Project " + root.getAbsolutePath() + " does not exist");
        }
        this.root = root;
    }

    public Project parse() {
        if (project == null) {
            // traverse project differently depending on project type ?
            // assume Maven project by default for now
            switch (projectType) {
                case MAVEN:
                    this.project = new MavenProject(root, walk(root));
                    break;
                default:
                    throw new RuntimeException("Unrecognized Project Type");
            }
        }
        return this.project;
    }

    private static List<TestCase> walk(File start) {
        File[] list = start.listFiles();
        List<TestCase> testCases = new ArrayList<>();

        for (File f : list) {
            if (f.isDirectory()) {
                testCases.addAll(walk(f));
            } else {
                if (f.getName().contains(".java")) {
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
        }
        return testCases;
    }

    private static boolean isTestSuite(String code) {
        return code.contains("junit") || code.contains("@Test");
    }

    public static CompilationUnit parseCompliationUnit(String fileContent) {

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
        PackageDeclaration packageDeclaration = unit.getPackage();
        String className;
        if (packageDeclaration == null) {
            className = retriever.getClassName();
        } else {
            className = unit.getPackage().getName() + "." + retriever.getClassName();
        }
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

    public static File getFileOfClass(String classCanonicalName, File start) {
        String[] packagePartsArr = classCanonicalName.split("[.]", -1);
        Set<String> packageParts = new HashSet<>(Arrays.asList(packagePartsArr));
        String packageName = "";
        for (int i = 0; i < packagePartsArr.length - 1; i++) {
            packageName += packagePartsArr[i];
            if (i == packagePartsArr.length - 2) {
                continue;
            }
            packageName += ".";
        }
        String className = packagePartsArr[packagePartsArr.length - 1].split("[$]", 2)[0];
        return getFileOfClassHelper(packageParts, packageName, className, start);
    }

    private static File getFileOfClassHelper(Set<String> packageParts, String packageName, String className, File start) {
        File[] list = start.listFiles();
        if (list == null) {
            return null;
        }
        for (File f : list) {
            if (f.isDirectory() && (f.getName().equals("src") || f.getName().equals("main") || f.getName().equals("java") || f.getName().equals("test") || packageParts.contains(f.getName()))) {
                File file = getFileOfClassHelper(packageParts, packageName, className, f);
                if (file != null) {
                    return file;
                }
            } else {
                if (f.getName().contains(".java")) {
                    try {
                        String fileContent = Files.readString(f.toPath());
                        if (fileContent.contains("package " + packageName) && fileContent.contains("class " + className)) {
                            return f;
                        }
                    } catch (IOException e) {
                        System.out.print("Unable to open file at ");
                        System.out.println(f.getAbsolutePath());
                    }
                }
            }
        }
        return null;
    }
}
