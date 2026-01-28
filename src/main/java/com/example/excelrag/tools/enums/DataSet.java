package com.example.excelrag.tools.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List; /**
 * 图表数据集
 */
public class DataSet {
    private final String label;
    private final List<Double> data;
    private final String color;
    private final boolean fill;

    @JsonCreator
    public DataSet(
            @JsonProperty("label") String label,
            @JsonProperty("data") List<Double> data,
            @JsonProperty("color") String color,
            @JsonProperty("fill") boolean fill) {
        this.label = label;
        this.data = data;
        this.color = color;
        this.fill = fill;
    }

    public String getLabel() { return label; }
    public List<Double> getData() { return data; }
    public String getColor() { return color; }
    public boolean isFill() { return fill; }
}
