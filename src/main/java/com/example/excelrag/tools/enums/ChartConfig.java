package com.example.excelrag.tools.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map; /**
 * 图表配置
 */
public class ChartConfig {
    private final String title;
    private final int width;
    private final int height;
    private final String theme;
    private final boolean showLegend;
    private final boolean showGrid;
    private final boolean interactive;
    private final Map<String, Object> customOptions;

    @JsonCreator
    public ChartConfig(
            @JsonProperty("title") String title,
            @JsonProperty("width") Integer width,
            @JsonProperty("height") Integer height,
            @JsonProperty("theme") String theme,
            @JsonProperty("showLegend") Boolean showLegend,
            @JsonProperty("showGrid") Boolean showGrid,
            @JsonProperty("interactive") Boolean interactive,
            @JsonProperty("customOptions") Map<String, Object> customOptions) {
        this.title = title != null ? title : "数据可视化图表";
        this.width = width != null ? width : 800;
        this.height = height != null ? height : 500;
        this.theme = theme != null ? theme : "default";
        this.showLegend = showLegend != null ? showLegend : true;
        this.showGrid = showGrid != null ? showGrid : true;
        this.interactive = interactive != null ? interactive : true;
        this.customOptions = customOptions != null ? customOptions : Map.of();
    }

    // Getters...
    public String getTitle() { return title; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getTheme() { return theme; }
    public boolean isShowLegend() { return showLegend; }
    public boolean isShowGrid() { return showGrid; }
    public boolean isInteractive() { return interactive; }
    public Map<String, Object> getCustomOptions() { return customOptions; }
}
