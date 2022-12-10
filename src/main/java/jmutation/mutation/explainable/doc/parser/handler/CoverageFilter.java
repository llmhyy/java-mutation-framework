package jmutation.mutation.explainable.doc.parser.handler;

import jmutation.execution.Coverage;
import jmutation.model.ast.ASTNodeRetriever;
import jmutation.model.mutation.MutationRange;
import jmutation.mutation.explainable.doc.model.JavaComment;
import jmutation.parser.ProjectParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CoverageFilter extends ProjectParserFilter {
    Set<String> classNamesInCoverage;

    public CoverageFilter(Coverage coverage) {
        classNamesInCoverage = new HashSet<>();
        List<MutationRange> ranges = coverage.getRanges();
        for (MutationRange range : ranges) {
            classNamesInCoverage.add(range.getClassName());
        }
    }

    @Override
    protected boolean privateHandle(JavaComment comment, Request request) {
        String source = "";
        try {
            source = Files.readString(Path.of(comment.getFilePath()));
        } catch (IOException e) {
            e.printStackTrace();
            request.setSuccessful(false);
            return false;
        }
        CompilationUnit unit = ProjectParser.parseCompilationUnit(source);
        List<String> classNames = getClassNames(unit);
        for (String className : classNames) {
            if (classNamesInCoverage.contains(className)) {
                return true;
            }
        }
        request.setSuccessful(false);
        return false;
    }

    private List<String> getClassNames(CompilationUnit unit) {
        ASTNodeRetriever<TypeDeclaration> typeDeclarationASTNodeRetriever = new ASTNodeRetriever<>(TypeDeclaration.class);
        unit.accept(typeDeclarationASTNodeRetriever);
        List<TypeDeclaration> typeDeclarations = typeDeclarationASTNodeRetriever.getNodes();
        PackageDeclaration packageDeclaration = unit.getPackage();
        String packageName = "";
        if (packageDeclaration != null) {
            packageName = packageDeclaration.getName().toString() + ".";
        }
        List<String> result = new ArrayList<>();
        for (TypeDeclaration typeDeclaration : typeDeclarations) {
            result.add(packageName + typeDeclaration.getName().toString());
        }
        return result;
    }
}
