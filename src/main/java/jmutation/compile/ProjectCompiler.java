package jmutation.compile;

import jmutation.model.TestCase;
import jmutation.model.ast.JdtMethodRetriever;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 
 * Given an arbitrary project (maven or gradle), we shall find a way to compile it.
 * 
 * @author Yun Lin
 *
 */
public class ProjectCompiler {
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
		for (MethodDeclaration node : methodNodes) {
			if (!(node.getParent().getParent() instanceof CompilationUnit) ){
				continue;
			}
			String simpleName = node.getName().toString();
			List<ASTNode> parameters = node.parameters();
			// SingleVariableDeclaration
			StringJoiner sj = new StringJoiner(",", simpleName + "(", ")");
			parameters.stream().forEach(param -> sj.add(param.toString()));
			String signature = sj.toString();

			int startLine = unit.getLineNumber(node.getStartPosition()) - 1;
			int endLine = unit.getLineNumber(node.getStartPosition() + node.getLength()) - 1;
			methods.add(new TestCase(signature, startLine, endLine, simpleName, node));
		}
		return methods;
	}
}
