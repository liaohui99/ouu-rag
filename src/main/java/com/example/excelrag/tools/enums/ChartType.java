package com.example.excelrag.tools.enums;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/1/27 20:21
 * @description: TODO
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.agent.tool.P;

import java.util.List;
import java.util.Map;

/**
 * 图表数据类型枚举
 */
public enum ChartType {
    LINE("折线图"),
    BAR("柱状图"),
    PIE("饼图"),
    DONUT("环形图"),
    SCATTER("散点图"),
    AREA("面积图"),
    RADAR("雷达图"),
    HEATMAP("热力图");

    private final String chineseName;

    ChartType(String chineseName) {
        this.chineseName = chineseName;
    }

    public String getChineseName() {
        return chineseName;
    }
}

