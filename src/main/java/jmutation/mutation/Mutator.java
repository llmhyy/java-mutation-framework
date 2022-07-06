package jmutation.mutation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import jmutation.mutation.commands.MutationCommand;
import jmutation.mutation.parser.MutationParser;
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
			
			String fileContent = retrieveFileFromClassName(className, newProject);
			System.out.println("File Content \n" + fileContent);

			CompilationUnit unit = ProjectParser.parseCompliationUnit(fileContent);
			unit.types();
			ASTNode node = parseRangeToNode(unit, range);
			MutationCommand mutationCommand = MutationParser.createMutationCommand(node);
			ASTNode mutatedNode = mutationCommand.executeMutation();
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


		}
		
		return newProject;
	}

	private String retrieveFileFromClassName(String className, Project newProject) {
		File root = newProject.getRoot();
		return ProjectParser.getFileContentsOfClass(className, root);
	}

	/**
	 * Gets an AST node for the portion of code to be mutated
	 * @param unit
	 * @param range
	 * @return
	 */
	private ASTNode parseRangeToNode(CompilationUnit unit, MutationRange range) {
		MinimumASTNodeRetriever retriever = new MinimumASTNodeRetriever(unit, range);
		unit.accept(retriever);

		return retriever.getNode();
	}
}
