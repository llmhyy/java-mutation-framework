package jmutation.mutation.explainable;

import jmutation.model.mutation.MutationRange;
import jmutation.model.project.Defects4jProject;
import jmutation.model.project.Project;
import jmutation.mutation.MutationCommand;
import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.Mutator;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExplainableMutatorIT {
    private ExplainableMutator explainableMutator;
    private MutationTestHelper helper;
    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        explainableMutator = new ExplainableMutator(null);
        helper = new MutationTestHelper();
    }

    @Test
    void analyse_FilesWithMethods_ReturnsCorrectCommands() throws IOException {
        String fileContent = String.join(System.lineSeparator(), "class ClassToMutate {",
                "int a = 0;",
                "/**",
                " * Some description",
                " */",
                "public void method() {",
                "int b = 0;",
                "}",
                "}");
        Path pathToFileToMutate = tempDir.resolve("ClassToMutate.java");
        List<MutationRange> mutationRanges = new ArrayList<>();
        MutationRange mutationRange = new MutationRange("ClassToMutate", 0, 0);
        mutationRanges.add(mutationRange);
        Files.writeString(pathToFileToMutate, fileContent);
        Project project = new Defects4jProject("name", tempDir.toFile(), null, ".", null, null, null);
        List<MutationCommand> mutationCommands = explainableMutator.analyse(mutationRanges, project);
        helper.parseDocStr(fileContent);
        BodyDeclaration bodyDeclaration = helper.getBodyDeclarations().get(1);
        MutationCommand expectedCommand = new ExplainableMutationCommand(bodyDeclaration, null);
        List<MutationCommand> expectedCommands = new ArrayList<>();
        expectedCommands.add(expectedCommand);
        assertEquals(expectedCommands, mutationCommands);
    }
}