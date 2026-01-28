package com.example.excelrag.tools.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty; /**
 * 图表数据点
 */
public  class DataPoint {
    private final String x;
    private final Double y;
    private final String category;

    @JsonCreator
    public DataPoint(
            @JsonProperty("x") String x,
            @JsonProperty("y") Double y,
            @JsonProperty("category") String category) {
        this.x = x;
        this.y = y;
        this.category = category;
    }

    public String getX() { return x; }
    public Double getY() { return y; }
    public String getCategory() { return category; }
}
