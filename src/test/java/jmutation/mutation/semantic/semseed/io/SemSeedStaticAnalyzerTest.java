package jmutation.mutation.semantic.semseed.io;

import jmutation.execution.Coverage;
import jmutation.model.MutationRange;
import jmutation.mutation.semantic.semseed.SemSeedStaticAnalyzer;
import jmutation.mutation.semantic.semseed.model.StaticAnalysisResult;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SemSeedStaticAnalyzerTest {
    @Test
    public void analyze_sampleProject_obtainsCorrectAnalysis() {
        Coverage coverage = new Coverage();
        List<MutationRange> ranges = new ArrayList<>();
        ranges.add(new MutationRange("Sample", 0, 0));
        coverage.setRanges(ranges);
        File root = new File(String.join(File.separator, "sample", "testng"));
        SemSeedStaticAnalyzer staticAnalyzer = new SemSeedStaticAnalyzer(coverage, root, 1);
        StaticAnalysisResult actualResult = staticAnalyzer.analyse();
        Set<String> expectedIdentifiersInFile = new HashSet<>(List.of("TestNGTest", "a", "sampleMethod", "test", "org", "Sample", "i", "annotations", "assertEquals", "test1", "Assert", "Test", "testng"));
        Map<String, Set<String>> expectedIdentifiers = new HashMap<>();
        expectedIdentifiers.put("Sample", expectedIdentifiersInFile);
        Map<String, Integer> expectedIdentifierCount = new HashMap<>();
        expectedIdentifierCount.put("a", 7);
        StaticAnalysisResult expectedResult = new StaticAnalysisResult(expectedIdentifierCount, new HashMap<>(), expectedIdentifiers, new HashMap<>());
        assertEquals(expectedResult, actualResult);
    }
}
