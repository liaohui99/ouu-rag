package com.example.excelrag.model.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.persistence.*;
import lombok.Data;


/**
 * 成交分析实体类
 * 用于存储Excel中的成交分析数据
 */
@Data
@Entity
@Table(name = "transaction_analysis")
public class TransactionAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ExcelProperty("日期")
    @Column(name = "transaction_date", nullable = true)
    private String transactionDate;

    @ExcelProperty("商品名称")
    @Column(name = "product_name", nullable = true)
    private String productName;

    @ExcelProperty("商品类别")
    @Column(name = "product_category", nullable = true)
    private String productCategory;

    @ExcelProperty("成交数量")
    @Column(name = "quantity", nullable = true)
    private Integer quantity;

    @ExcelProperty("成交金额")
    @Column(name = "amount", nullable = true)
    private Double amount;

    @ExcelProperty("客户名称")
    @Column(name = "customer_name", nullable = true)
    private String customerName;

    @ExcelProperty("客户类型")
    @Column(name = "customer_type", nullable = true)
    private String customerType;

    @ExcelProperty("销售渠道")
    @Column(name = "sales_channel", nullable = true)
    private String salesChannel;

    @ExcelProperty("销售人员")
    @Column(name = "sales_person", nullable = true)
    private String salesPerson;

    @ExcelProperty("地区")
    @Column(name = "region", nullable = true)
    private String region;

    @ExcelProperty("备注")
    @Column(name = "remark", nullable = true)
    private String remark;
}
