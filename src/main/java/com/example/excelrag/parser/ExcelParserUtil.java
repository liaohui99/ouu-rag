package com.example.excelrag.parser;

import com.alibaba.excel.EasyExcel;
import com.example.excelrag.model.excel.ExcelCellData;

import java.io.File;
import java.util.List;

public class ExcelParserUtil {

    public static List<ExcelCellData> parseExcel(File excelFile, int headRowNumber) {
        String fileName = excelFile.getName();
        ExcelAnalysisListener listener = new ExcelAnalysisListener(fileName);
        EasyExcel.read(excelFile)
                .registerReadListener(listener)
                .headRowNumber(headRowNumber)
                .doReadAll();
        return listener.getCellDataList();
    }

    public static List<ExcelCellData> parseExcel(File excelFile) {
        return parseExcel(excelFile, 0);
    }
}
