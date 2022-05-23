package jmutation.model;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class TestCase {
    public final String signature;
    public final int startLine;
    public final int stopLine;
    public final String simpleName;
    public final MethodDeclaration mtd;

    public TestCase(String signature, int startLine, int stopLine, String simpleName, MethodDeclaration mtd)
    {
        this.signature = signature;
        this.startLine = startLine;
        this.stopLine = stopLine;
        this.simpleName = simpleName;
        this.mtd = mtd;
    }
}
