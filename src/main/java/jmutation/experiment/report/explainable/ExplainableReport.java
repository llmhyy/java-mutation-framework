package jmutation.experiment.report.explainable;

import jmutation.experiment.excel.AbstractExcelWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.io.IOException;

import static jmutation.experiment.report.explainable.ExplainableTrialHeader.*;

public class ExplainableReport extends AbstractExcelWriter<ExplainableTrial> {
    public static final String FAILING_TESTS_DELIMITER = ", ";
    public ExplainableReport(File file) throws IOException {
        super(file);
    }

    public void record(ExplainableTrial explainableTrial) throws IOException {
        Sheet sheet = getSheet("mutations", ExplainableTrialHeader.values(), 0);
        int rowNum = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(rowNum);
        writeTrial(row, explainableTrial);
        writeWorkbook();
    }

    // (project name, version, old method, new method, old comment, new comment,
    // message, failing tests, total test count), command
    private void writeTrial(Row row, ExplainableTrial trial) {
        addCell(row, PROJECT_NAME, trial.getProjectName());
        addCell(row, MUTATION_COMMAND, trial.getCommand());
        addCell(row, VERSION, trial.getVersion());
        addCell(row, COMMENT, trial.getComment());
        addCell(row, MUTATED_COMMENT, trial.getMutatedComment());
        addCell(row, METHOD, trial.getMethod());
        addCell(row, MUTATED_METHOD, trial.getMutatedMethod());
        addCell(row, MESSAGE, trial.getMessage());
        StringBuilder builder = new StringBuilder();
        for(String test : trial.getFailingTests()) {
            builder.append(test + FAILING_TESTS_DELIMITER);
        }
        builder.delete(builder.length()-2, builder.length());
        addCell(row, FAILING_TESTS, builder.toString());
        addCell(row, TOTAL_TEST_COUNT, trial.getTotalTestCount());
    }
}
