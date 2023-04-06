package jmutation.experiment.report.mutation;

import jmutation.mutation.MutationCommand;
import jmutation.experiment.excel.AbstractExcelWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.io.IOException;

import static jmutation.experiment.report.mutation.MutationTrialHeader.*;

/**
 * @author LLT
 */
public class MutationReport extends AbstractExcelWriter<MutationTrial> {

    public MutationReport(File file) throws Exception {
        super(file);
    }

    public void record(MutationTrial trial) throws IOException {
        Sheet sheet = getSheet("mutations", MutationTrialHeader.values(), 0);
        int rowNum = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(rowNum);
        writeTestcase(row, trial);
        writeWorkbook();
    }

    private void writeTestcase(Row row, MutationTrial trial) {
        addCell(row, PROJECT_NAME, trial.getProjectName());
        addCell(row, TEST_CASE, trial.getTestCase().toString());
        MutationCommand command = trial.getMutationCommand();
        addCell(row, MUTATION_COMMAND, command == null ? "null" : command.toString());
        addCell(row, PROGRAM_MSG, trial.getProgramMsg());
        addCell(row, MUTATED_PROGRAM_MSG, trial.getMutatedProgramMsg());
        addCell(row, TOTAL_STEPS, trial.getTraceLength());
        addCell(row, MUTATED_TOTAL_STEPS, trial.getMutatedTraceLength());
    }


}
