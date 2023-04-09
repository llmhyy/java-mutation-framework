/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package jmutation.experiment.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author LLT
 */
abstract public class AbstractExcelWriter<T> {
    protected Workbook workbook;
    private File file;

    public AbstractExcelWriter(File file) throws IOException {
        reset(file);
    }

    public void reset(File file) throws IOException {
        this.file = file;
        if (!file.exists()) {
            initFromNewFile(file);
            writeWorkbook();
        } else {
            initFromExistingFile(file);
        }
    }

    protected void initFromExistingFile(File file) throws IOException {
        InputStream inp = new FileInputStream(file);
        workbook = WorkbookFactory.create(inp);
    }

    protected void initFromNewFile(File file) {
        workbook = new XSSFWorkbook();
    }

    public Sheet createSheet(String name) {
        return workbook.createSheet(name);
    }

    public Sheet createSheet(String name, ExcelHeader[] headers, int headerRowIdx) {
        Sheet sheet = createSheet(name);
        initDataSheetHeader(sheet, headers, headerRowIdx);
        return sheet;
    }

    public void initDataSheetHeader(Sheet sheet, ExcelHeader[] headers, int headerRowIdx) {
        Row headerRow = newDataSheetRow(sheet, headerRowIdx);
        for (ExcelHeader header : headers) {
            addCell(headerRow, header, header.getTitle());
        }
    }

    protected Row newDataSheetRow(Sheet dataSheet, int headerRowIdx) {
        return dataSheet.createRow(headerRowIdx);
    }

    public void writeWorkbook() throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            workbook.write(out);
        }
    }

    public void addCell(Row row, ExcelHeader title, double value) {
        row.createCell(title.getCellIdx()).setCellValue(value);
    }

    public void addCell(Row row, ExcelHeader title, String value) {
        row.createCell(title.getCellIdx()).setCellValue(value);
    }

    public void addCell(Row row, ExcelHeader title, boolean value) {
        row.createCell(title.getCellIdx()).setCellValue(value);
    }

    public Sheet getSheet(String name, ExcelHeader[] headers, int headerRowIdx) {
        Sheet sheet = workbook.getSheet(name);
        if (sheet == null) {
            sheet = createSheet(name, headers, headerRowIdx);
        }
        return sheet;
    }
    public abstract void record(T trial) throws IOException;
}
