/**
 *
 */
package jmutation.report;

import jmutation.report.excel.ExcelHeader;

/**
 * Headers for report on number of mutations and types possible in each test case
 */
public enum MutationTrialHeader implements ExcelHeader {
    PROJECT_NAME,
    TEST_CASE,
    MUTATION_COMMAND,
    PROGRAM_MSG,
    MUTATED_PROGRAM_MSG,
    TOTAL_STEPS,
    MUTATED_TOTAL_STEPS;

    @Override
    public String getTitle() {
        return name();
    }

    @Override
    public int getCellIdx() {
        return ordinal();
    }

}
