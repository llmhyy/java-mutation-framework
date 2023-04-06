package jmutation.experiment.report.explainable;

import jmutation.experiment.excel.AbstractExcelWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.io.IOException;

import static jmutation.experiment.report.explainable.ExplainableTrialHeader.*;

public class ExplainableReport extends AbstractExcelWriter<ExplainableTrial> {
    public ExplainableReport(File file) throws Exception {
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
        addCell(row, OLD_COMMENT, trial.getOldComment());
        addCell(row, NEW_COMMENT, trial.getNewComment());
        addCell(row, OLD_METHOD, trial.getOldMethod());
        addCell(row, NEW_METHOD, trial.getNewMethod());
        addCell(row, MESSAGE, trial.getMessage());
        StringBuilder builder = new StringBuilder();
        for(String test : trial.getFailingTests()) {
            builder.append(test + ", ");
        }
        builder.delete(builder.length()-2, builder.length());
        addCell(row, FAILING_TESTS, builder.toString());
        addCell(row, TOTAL_TEST_COUNT, trial.getTotalTestCount());
    }
}
