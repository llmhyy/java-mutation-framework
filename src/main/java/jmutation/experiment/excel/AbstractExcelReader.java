package jmutation.experiment.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Iterator;

abstract public class AbstractExcelReader<T> {
    protected final XSSFWorkbook workbook;

    public AbstractExcelReader(XSSFWorkbook workbook) {
        this.workbook = workbook;
    }

    // TODO: Implement value type in ExcelHeader class. Currently it obtains cell value as a string/int based on method call.
    public String getCellString(Row row, ExcelHeader header) {
        return row.getCell(header.getCellIdx()).getStringCellValue();
    }

    public int getCellInt(Row row, ExcelHeader header) {
        return (int) row.getCell(header.getCellIdx()).getNumericCellValue();
    }

    public abstract Iterator<T> getTrials();
}
