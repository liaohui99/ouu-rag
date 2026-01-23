package com.example.excelrag.utils.excel;

import com.alibaba.excel.EasyExcel;
import com.example.excelrag.model.excel.ExcelCellData;
import com.example.excelrag.parser.ExcelAnalysisListener;

import java.io.File;
import java.util.List;

/**
 * Excel通用解析工具类
 * ✅ 适配.xls/.xlsx | ✅ 支持表头配置 | ✅ 自动过滤空单元格 | ✅ 无内存溢出
 */
public class ExcelParserUtil {

    /**
     * 通用Excel解析方法（推荐）
     * @param excelFile 待解析的Excel文件
     * @param headRowNumber 表头行数【核心】：有表头填1，纯数据无表头填0
     * @return 所有有效单元格数据列表（已过滤空值）
     */
    public static List<ExcelCellData> parseExcel(File excelFile, int headRowNumber) {
        String fileName = excelFile.getName();
        ExcelAnalysisListener listener = new ExcelAnalysisListener(fileName);
        // EasyExcel 3.x 标准写法，一行代码完成全表读取
        EasyExcel.read(excelFile)
                .registerReadListener(listener) // 注册监听器接收解析数据
                .headRowNumber(headRowNumber)   // 指定表头行数，避免表头混入数据
                .doReadAll();                    // 读取Excel中所有工作表

        return listener.getCellDataList();
    }

    /**
     * 重载方法：默认无表头（headRowNumber=0），简化无表头Excel调用
     */
    public static List<ExcelCellData> parseExcel(File excelFile) {
        return parseExcel(excelFile, 0);
    }
}

