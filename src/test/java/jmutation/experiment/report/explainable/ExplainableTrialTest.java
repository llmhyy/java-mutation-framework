package jmutation.experiment.report.explainable;

import jmutation.mutation.explainable.ExplainableMutationCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExplainableTrialTest {
    @Nested
    class ExplainableTrialFactoryTest {
        private ExplainableTrial.ExplainableTrialFactory factory;
        @BeforeEach
        void setUp() {
            factory = new ExplainableTrial.ExplainableTrialFactory();
        }

        @Test
        void create_ValidArguments_CreatesCorrectTrial() {
            String projName = "projectName";
            String originalMethod = "originalMethod";
            String mutatedMethod = "mutatedMethod";
            String originalComment = "originalComment";
            String mutatedComment = "mutatedComment";
            String commandToString = "commandToString";
            String version = "version";
            String message = "message";
            String[] failingTests = new String[] {"failingTests0"};
            int total = 1;
            ExplainableMutationCommand command = mock(ExplainableMutationCommand.class);
            when(command.getOriginalComment()).thenReturn(originalComment);
            when(command.getOriginalMethod()).thenReturn(originalMethod);
            when(command.getMutatedComment()).thenReturn(mutatedComment);
            when(command.getMutatedMethod()).thenReturn(mutatedMethod);
            when(command.toString()).thenReturn(commandToString);
            ExplainableTrial actual = factory.create(projName, version, command, message, failingTests, 1);
            ExplainableTrial expected = new ExplainableTrial(projName, version, originalMethod, mutatedMethod, originalComment, mutatedComment,
                    message, commandToString, failingTests, total);
            assertEquals(expected, actual);
        }
    }

}