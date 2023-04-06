package jmutation.experiment.report.explainable;

import jmutation.experiment.excel.ExcelHeader;
// (project name, version, old method, new method, old comment, new comment,
// message, failing tests, total test count)
public enum ExplainableTrialHeader implements ExcelHeader {
    PROJECT_NAME,
    VERSION,
    OLD_METHOD,
    NEW_METHOD,
    OLD_COMMENT,
    NEW_COMMENT,
    MESSAGE,
    FAILING_TESTS,
    TOTAL_TEST_COUNT,
    MUTATION_COMMAND;

    @Override
    public String getTitle() {
        return name();
    }

    @Override
    public int getCellIdx() {
        return ordinal();
    }
}
