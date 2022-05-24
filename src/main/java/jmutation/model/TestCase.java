package jmutation.model;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestCase {
    public final String signature;
    public final int startLine;
    public final int stopLine;
    public final String simpleName;
    public final String testClass;
    public final MethodDeclaration mtd;

    public TestCase(String signature, int startLine, int stopLine, String simpleName, String testClass, MethodDeclaration mtd)
    {
        this.signature = signature;
        this.startLine = startLine;
        this.stopLine = stopLine;
        this.simpleName = simpleName;
        this.testClass = testClass;
        this.mtd = mtd;
    }

    @Override
    public String toString() {
        return Stream.of(String.format("%s#%s", this.testClass, this.signature), Integer.toString(this.startLine), Integer.toString(this.stopLine)).collect(Collectors.joining(","));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCase testCase = (TestCase) o;
        return startLine == testCase.startLine && stopLine == testCase.stopLine && signature.equals(testCase.signature) && simpleName.equals(testCase.simpleName) && testClass.equals(testCase.testClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signature, startLine, stopLine, simpleName, mtd);
    }
}
