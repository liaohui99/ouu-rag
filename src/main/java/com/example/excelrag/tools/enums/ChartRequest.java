package com.example.excelrag.tools.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List; /**
 * 图表生成请求
 */
public class ChartRequest {
    private final ChartType chartType;
    private final List<String> labels;
    private final List<DataSet> datasets;
    private final ChartConfig config;

    @JsonCreator
    public ChartRequest(
            @JsonProperty("chartType") ChartType chartType,
            @JsonProperty("labels") List<String> labels,
            @JsonProperty("datasets") List<DataSet> datasets,
            @JsonProperty("config") ChartConfig config) {
        this.chartType = chartType;
        this.labels = labels;
        this.datasets = datasets;
        this.config = config != null ? config : new ChartConfig(null, null, null, null, null, null, null, null);
    }

    public ChartType getChartType() { return chartType; }
    public List<String> getLabels() { return labels; }
    public List<DataSet> getDatasets() { return datasets; }
    public ChartConfig getConfig() { return config; }
}
