package com.example.excelrag.model.excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelCellData {
    /** 文件名称 */
    private String fileName;
    /** 工作表名称（如：Sheet1、用户信息表） */
    private String sheetName;
    /** 行号（从1开始，与Excel视觉行号一致） */
    private Integer rowNum;
    /** 列号（从1开始，与Excel视觉列号一致） */
    private Integer colNum;
    /** 列名（表头） */
    private String columnName;
    /** 单元格有效值（已去空、去首尾空格） */
    private String cellValue;
}
