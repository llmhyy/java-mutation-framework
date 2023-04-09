package jmutation.experiment.report.explainable;

import jmutation.experiment.excel.AbstractExcelReader;
import jmutation.experiment.excel.ExcelHeader;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExplainableReportReader extends AbstractExcelReader<ExplainableTrial> {
    public ExplainableReportReader(XSSFWorkbook workbook) {
        super(workbook);
    }

    @Override
    public Iterator<ExplainableTrial> getTrials() {
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();
        rowIterator.next(); // Skip header row
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return rowIterator.hasNext();
            }

            @Override
            public ExplainableTrial next() {
                Row row = rowIterator.next();
                Map<ExcelHeader, String> mapOfTrialValues = new HashMap<>();
                int totalCount = 0;
                for (ExplainableTrialHeader header : ExplainableTrialHeader.values()) {
                    switch (header) {
                        case TOTAL_TEST_COUNT:
                            totalCount = getCellInt(row, header);
                            break;
                        default:
                            String cellContents = getCellString(row, header);
                            mapOfTrialValues.put(header, cellContents);
                    }
                }
                String[] failingTests = mapOfTrialValues.get(ExplainableTrialHeader.FAILING_TESTS).
                        split(ExplainableReport.FAILING_TESTS_DELIMITER);
                return new ExplainableTrial(mapOfTrialValues.get(ExplainableTrialHeader.PROJECT_NAME),
                        mapOfTrialValues.get(ExplainableTrialHeader.VERSION),
                        mapOfTrialValues.get(ExplainableTrialHeader.METHOD),
                        mapOfTrialValues.get(ExplainableTrialHeader.MUTATED_METHOD),
                        mapOfTrialValues.get(ExplainableTrialHeader.COMMENT),
                        mapOfTrialValues.get(ExplainableTrialHeader.MUTATED_COMMENT),
                        mapOfTrialValues.get(ExplainableTrialHeader.MESSAGE),
                        mapOfTrialValues.get(ExplainableTrialHeader.MUTATION_COMMAND),
                        failingTests,
                        totalCount);
            }
        };
    }
}
