package jmutation.mutation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import jmutation.execution.Coverage;
import jmutation.model.Project;
import jmutation.parser.ProjectParser;

/**
 * 
 * Given an arbitrary project (maven or gradle) and a region, we shall mutate the region into a compilable project.
 * 
 * @author Yun Lin
 *
 */
public class Mutator {
	public Project mutate(Coverage coverage, Project project) {
		
		// copy a project
		Project newProject = project.cloneToOtherPath();
		
		List<MutationRange> ranges = coverage.getRanges();
		for(MutationRange range: ranges) {
			String className = range.getClassName();
			
			String fileLocation = retrieveFileFromClassName(className, newProject);
			String fileContent = null;
			try {
				fileContent = Files.readString(new File(fileLocation).toPath());
				
				CompilationUnit unit = ProjectParser.parseCompliationUnit(fileContent);
				
				ASTNode node = parseRangeToNode(unit, range);
				
				/**
				 * TODO: 
				 * 
				 * check https://www.ibm.com/docs/en/rational-soft-arch/9.5?topic=SS8PJ7_9.5.0/org.eclipse.jdt.doc.isv/reference/api/org/eclipse/jdt/core/dom/rewrite/ASTRewrite.html
				 * https://www.eclipse.org/articles/article.php?file=Article-JavaCodeManipulation_AST/index.html
				 * to rewrite the AST
				 */
				
				// step 1: define mutation operator based on AST node
				// step 2: apply mutation on the AST node
				// step 3: rewrite the AST node back to Java doc
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		return newProject;
	}

	private String retrieveFileFromClassName(String className, Project newProject) {
		// TODO Auto-generated method stub
		return null;
	}

	private ASTNode parseRangeToNode(CompilationUnit unit, MutationRange range) {
		
		MinimumASTNodeRetriever retriever = new MinimumASTNodeRetriever(unit, range);
		unit.accept(retriever);

		return retriever.getNode();
	}
}
