package com.example.excelrag.tools;
/**
 * @author Gabriel
 * @date 2026/1/27 20:24
 * @description: TODO
 * @version 1.0
 */

import com.example.excelrag.tools.chart.ChartHtmlGenerator;
import com.example.excelrag.tools.enums.ChartConfig;
import com.example.excelrag.tools.enums.ChartRequest;
import com.example.excelrag.tools.enums.ChartType;
import com.example.excelrag.tools.enums.DataSet;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 修正版：数据可视化图表工具 - 供Agent调用
 * 修复了 argumentsAsMap() 方法不存在的问题
 */
@Component
public class DataVisualizationTool {

    private static final Logger logger = LoggerFactory.getLogger(DataVisualizationTool.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final ChartHtmlGenerator chartGenerator;

    public DataVisualizationTool() {
        this.chartGenerator = new ChartHtmlGenerator();
    }

    /**
     * 工具1：生成图表（完整版）
     */
    @Tool("Generate a data visualization chart based on the provided data. " +
            "Returns HTML that can be directly rendered in a browser. " +
            "Supported chart types: LINE, BAR, PIE, DONUT, SCATTER, AREA, RADAR, HEATMAP. " +
            "The datasets parameter should be a JSON string containing an array of datasets.")
    public String generateChart(
            @dev.langchain4j.agent.tool.P("Chart type (LINE, BAR, PIE, DONUT, SCATTER, AREA, RADAR, HEATMAP)")
            String chartType,
            @dev.langchain4j.agent.tool.P("Labels for X-axis or categories (comma-separated or JSON array)")
            String labels,
            @dev.langchain4j.agent.tool.P("Datasets in JSON format: [{\"label\":\"Series1\",\"data\":[1,2,3],\"color\":\"rgba(255,99,132,0.2)\",\"fill\":true}]")
            String datasetsJson,
            @dev.langchain4j.agent.tool.P("Chart title") String title,
            @dev.langchain4j.agent.tool.P("Chart width in pixels (default: 800)") Integer width,
            @dev.langchain4j.agent.tool.P("Chart height in pixels (default: 500)") Integer height,
            @dev.langchain4j.agent.tool.P("Theme (default or dark)") String theme) {

        try {
            // 参数校验
            if (chartType == null || chartType.trim().isEmpty()) {
                return generateErrorHtml("图表类型不能为空");
            }
            if (labels == null || labels.trim().isEmpty()) {
                return generateErrorHtml("标签不能为空");
            }
            if (datasetsJson == null || datasetsJson.trim().isEmpty()) {
                return generateErrorHtml("数据集不能为空");
            }

            // 解析输入参数
            ChartType type = ChartType.valueOf(chartType.toUpperCase());

            List<String> labelList;
            if (labels.startsWith("[")) {
                // JSON数组格式
                labelList = OBJECT_MAPPER.readValue(labels, List.class);
            } else {
                // 逗号分隔格式
                labelList = Arrays.asList(labels.split(","));
            }

            List<DataSet> datasets = parseDatasetsJson(datasetsJson);

            // 构建配置
            ChartConfig config = new ChartConfig(
                    title != null ? title : "数据可视化图表",
                    width != null ? width : 800,
                    height != null ? height : 500,
                    theme != null ? theme : "default",
                    true,
                    true,
                    true,
                    Map.of()
            );

            // 构建请求
            ChartRequest request = new ChartRequest(type, labelList, datasets, config);

            // 生成HTML
            String html = chartGenerator.generateFullHtml(request);

            logger.info("成功生成 {} 图表: {}", type.getChineseName(), config.getTitle());
            return html;

        } catch (IllegalArgumentException e) {
            logger.error("图表类型不支持: {}", chartType, e);
            return generateErrorHtml("不支持的图表类型: " + chartType + "。支持的类型: LINE, BAR, PIE, DONUT, SCATTER, AREA, RADAR, HEATMAP");
        } catch (Exception e) {
            logger.error("生成图表失败", e);
            return generateErrorHtml("生成图表失败: " + e.getMessage());
        }
    }

    /**
     * 工具2：快速生成柱状图
     */
    @Tool("Quickly generate a bar chart with simplified parameters. " +
            "Perfect for comparing values across different categories.")
    public String generateBarChart(
            @dev.langchain4j.agent.tool.P("Chart title") String title,
            @dev.langchain4j.agent.tool.P("Category labels (comma-separated)") String categories,
            @dev.langchain4j.agent.tool.P("Values for each category (comma-separated numbers)") String values,
            @dev.langchain4j.agent.tool.P("Bar color in CSS format (default: rgba(54, 162, 235, 0.5)") String color) {

        try {
            // 参数校验
            if (categories == null || categories.trim().isEmpty()) {
                return generateErrorHtml("类别标签不能为空");
            }
            if (values == null || values.trim().isEmpty()) {
                return generateErrorHtml("数值不能为空");
            }

            List<String> labels = Arrays.asList(categories.split(","));
            List<Double> dataValues = Arrays.stream(values.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Double::parseDouble)
                    .toList();

            DataSet dataset = new DataSet(
                    "数据系列",
                    dataValues,
                    color != null ? color : "rgba(54, 162, 235, 0.5)",
                    false
            );

            ChartRequest request = new ChartRequest(
                    ChartType.BAR,
                    labels,
                    List.of(dataset),
                    new ChartConfig(title != null ? title : "柱状图", 800, 500, "default", true, true, true, Map.of())
            );

            return chartGenerator.generateFullHtml(request);

        } catch (NumberFormatException e) {
            logger.error("数值格式错误", e);
            return generateErrorHtml("数值格式错误: " + e.getMessage());
        } catch (Exception e) {
            logger.error("生成柱状图失败", e);
            return generateErrorHtml("生成柱状图失败: " + e.getMessage());
        }
    }

    /**
     * 工具3：生成饼图
     */
    @Tool("Generate a pie chart to show proportions. " +
            "Perfect for displaying percentage distributions.")
    public String generatePieChart(
            @dev.langchain4j.agent.tool.P("Chart title") String title,
            @dev.langchain4j.agent.tool.P("Labels for each segment (comma-separated)") String labels,
            @dev.langchain4j.agent.tool.P("Values for each segment (comma-separated numbers)") String values,
            @dev.langchain4j.agent.tool.P("Color palette (default: Chart.js default)") String colors) {

        try {
            // 参数校验
            if (labels == null || labels.trim().isEmpty()) {
                return generateErrorHtml("标签不能为空");
            }
            if (values == null || values.trim().isEmpty()) {
                return generateErrorHtml("数值不能为空");
            }

            List<String> labelList = Arrays.asList(labels.split(","));
            List<Double> valueList = Arrays.stream(values.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Double::parseDouble)
                    .toList();

            // 如果提供了颜色，使用自定义颜色；否则使用默认颜色方案
            String datasetColor = colors != null ? colors :
                    "['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40']";

            DataSet dataset = new DataSet(
                    "分布",
                    valueList,
                    datasetColor,
                    false
            );

            ChartRequest request = new ChartRequest(
                    ChartType.PIE,
                    labelList,
                    List.of(dataset),
                    new ChartConfig(title != null ? title : "饼图", 600, 600, "default", true, false, true, Map.of())
            );

            return chartGenerator.generateFullHtml(request);

        } catch (NumberFormatException e) {
            logger.error("数值格式错误", e);
            return generateErrorHtml("数值格式错误: " + e.getMessage());
        } catch (Exception e) {
            logger.error("生成饼图失败", e);
            return generateErrorHtml("生成饼图失败: " + e.getMessage());
        }
    }

    /**
     * 工具4：生成时间序列折线图
     */
    @Tool("Generate a time series line chart with multiple data series. " +
            "Perfect for showing trends over time.")
    public String generateLineChartTimeseries(
            @dev.langchain4j.agent.tool.P("Chart title") String title,
            @dev.langchain4j.agent.tool.P("Time labels (comma-separated dates or times)") String timeLabels,
            @dev.langchain4j.agent.tool.P("JSON string of datasets with names and values") String datasetsJson,
            @dev.langchain4j.agent.tool.P("Show data points (default: true)") Boolean showPoints,
            @dev.langchain4j.agent.tool.P("Fill area under lines (default: false)") Boolean fillArea) {

        try {
            // 参数校验
            if (timeLabels == null || timeLabels.trim().isEmpty()) {
                return generateErrorHtml("时间标签不能为空");
            }
            if (datasetsJson == null || datasetsJson.trim().isEmpty()) {
                return generateErrorHtml("数据集不能为空");
            }

            List<String> labels = Arrays.asList(timeLabels.split(","));
            List<DataSet> datasets = parseDatasetsJson(datasetsJson);

            // 如果需要填充区域，更新数据集
            if (fillArea != null && fillArea) {
                datasets = datasets.stream()
                        .map(ds -> new DataSet(
                                ds.getLabel(),
                                ds.getData(),
                                ds.getColor(),
                                true
                        ))
                        .toList();
            }

            Map<String, Object> customOptions = new HashMap<>();
            if (showPoints != null && !showPoints) {
                customOptions.put("pointRadius", 0);
            }

            ChartConfig config = new ChartConfig(
                    title != null ? title : "折线图", 1000, 500, "default", true, true, true, customOptions
            );

            ChartRequest request = new ChartRequest(
                    ChartType.LINE,
                    labels,
                    datasets,
                    config
            );

            return chartGenerator.generateFullHtml(request);

        } catch (Exception e) {
            logger.error("生成折线图失败", e);
            return generateErrorHtml("生成折线图失败: " + e.getMessage());
        }
    }

    /**
     * 工具5：生成内联图表片段
     */
    @Tool("Generate an inline chart HTML fragment that can be embedded in an existing web page. " +
            "Returns only the chart div and script, not a full HTML page.")
    public String generateInlineChart(
            @dev.langchain4j.agent.tool.P("Chart type (LINE, BAR, PIE, DONUT)") String chartType,
            @dev.langchain4j.agent.tool.P("Labels for X-axis or categories") String labels,
            @dev.langchain4j.agent.tool.P("Values for each category (comma-separated numbers)") String values,
            @dev.langchain4j.agent.tool.P("Chart title") String title,
            @dev.langchain4j.agent.tool.P("Width in pixels") Integer width,
            @dev.langchain4j.agent.tool.P("Height in pixels") Integer height) {

        try {
            // 参数校验
            if (chartType == null || chartType.trim().isEmpty()) {
                return generateErrorHtml("图表类型不能为空");
            }
            if (labels == null || labels.trim().isEmpty()) {
                return generateErrorHtml("标签不能为空");
            }
            if (values == null || values.trim().isEmpty()) {
                return generateErrorHtml("数值不能为空");
            }

            ChartType type = ChartType.valueOf(chartType.toUpperCase());
            List<String> labelList = Arrays.asList(labels.split(","));
            List<Double> dataValues = Arrays.stream(values.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Double::parseDouble)
                    .toList();

            DataSet dataset = new DataSet(
                    "数据",
                    dataValues,
                    "rgba(54, 162, 235, 0.5)",
                    false
            );

            ChartRequest request = new ChartRequest(
                    type,
                    labelList,
                    List.of(dataset),
                    new ChartConfig(title != null ? title : "图表", width != null ? width : 800, height != null ? height : 500, "default", true, true, true, Map.of())
            );

            return chartGenerator.generateInlineHtml(request);

        } catch (NumberFormatException e) {
            logger.error("数值格式错误", e);
            return generateErrorHtml("数值格式错误: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("图表类型不支持: {}", chartType, e);
            return generateErrorHtml("不支持的图表类型: " + chartType);
        } catch (Exception e) {
            logger.error("生成内联图表失败", e);
            return generateErrorHtml("生成内联图表失败: " + e.getMessage());
        }
    }

    /**
     * 获取工具规格
     */
    public List<ToolSpecification> getToolSpecifications() {
        return ToolSpecifications.toolSpecificationsFrom(this);
    }

    /**
     * 修正版：执行工具请求 - 手动解析参数
     */
    public String executeTool(ToolExecutionRequest request) {
        try {
            String toolName = request.name();
            String arguments = request.arguments();

            logger.debug("执行工具: {}, 参数: {}", toolName, arguments);

            // 解析JSON参数
            Map<String, Object> args = parseArguments(arguments);

            switch (toolName) {
                case "generate_chart":
                    return generateChart(
                            getString(args, "chartType"),
                            getString(args, "labels"),
                            getString(args, "datasetsJson"),
                            getString(args, "title"),
                            getInteger(args, "width"),
                            getInteger(args, "height"),
                            getString(args, "theme")
                    );

                case "generate_bar_chart":
                    return generateBarChart(
                            getString(args, "title"),
                            getString(args, "categories"),
                            getString(args, "values"),
                            getString(args, "color")
                    );

                case "generate_pie_chart":
                    return generatePieChart(
                            getString(args, "title"),
                            getString(args, "labels"),
                            getString(args, "values"),
                            getString(args, "colors")
                    );

                case "generate_line_chart_timeseries":
                    return generateLineChartTimeseries(
                            getString(args, "title"),
                            getString(args, "timeLabels"),
                            getString(args, "datasetsJson"),
                            getBoolean(args, "showPoints"),
                            getBoolean(args, "fillArea")
                    );

                case "generate_inline_chart":
                    return generateInlineChart(
                            getString(args, "chartType"),
                            getString(args, "labels"),
                            getString(args, "values"),
                            getString(args, "title"),
                            getInteger(args, "width"),
                            getInteger(args, "height")
                    );

                default:
                    logger.warn("未知的工具: {}", toolName);
                    return generateErrorHtml("未知的工具: " + toolName);
            }
        } catch (Exception e) {
            logger.error("执行工具失败", e);
            return generateErrorHtml("工具执行失败: " + e.getMessage());
        }
    }

    /**
     * 解析参数JSON字符串
     */
    private Map<String, Object> parseArguments(String argumentsJson) throws Exception {
        if (argumentsJson == null || argumentsJson.trim().isEmpty()) {
            return new HashMap<>();
        }

        try {
            // LangChain4j 传递的参数可能是JSON对象
            return OBJECT_MAPPER.readValue(
                    argumentsJson,
                    new TypeReference<Map<String, Object>>() {}
            );
        } catch (Exception e) {
            // 如果不是标准JSON，尝试其他格式
            logger.warn("参数不是标准JSON，尝试其他解析方式: {}", argumentsJson);

            // 尝试简单格式解析（例如：key1=value1, key2=value2）
            Map<String, Object> result = new HashMap<>();
            String[] pairs = argumentsJson.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    result.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
            return result;
        }
    }

    /**
     * 辅助方法：从Map获取字符串值
     */
    private String getString(Map<String, Object> args, String key) {
        Object value = args.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 辅助方法：从Map获取整数值
     */
    private Integer getInteger(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (value == null) return null;

        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    /**
     * 辅助方法：从Map获取布尔值
     */
    private Boolean getBoolean(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (value == null) return null;

        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            return Boolean.parseBoolean(value.toString());
        }
    }

    /**
     * 辅助方法：解析数据集JSON
     */
    private List<DataSet> parseDatasetsJson(String datasetsJson) throws Exception {
        if (datasetsJson == null || datasetsJson.trim().isEmpty()) {
            return List.of();
        }

        try {
            // 尝试解析为JSON数组
            List<Map<String, Object>> datasetsList = OBJECT_MAPPER.readValue(
                    datasetsJson,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, Map.class)
            );

            return datasetsList.stream()
                    .map(map -> {
                        String label = map.containsKey("label") ? map.get("label").toString() : "数据系列";
                        
                        List<Double> data = new ArrayList<>();
                        if (map.containsKey("data")) {
                            Object dataObj = map.get("data");
                            if (dataObj instanceof List) {
                                data = ((List<?>) dataObj).stream()
                                        .map(item -> {
                                            if (item instanceof Number) {
                                                return ((Number) item).doubleValue();
                                            } else {
                                                return Double.parseDouble(item.toString());
                                            }
                                        })
                                        .toList();
                            }
                        }
                        
                        String color = map.containsKey("color") ? map.get("color").toString() : "rgba(54, 162, 235, 0.5)";
                        boolean fill = map.containsKey("fill") ? Boolean.parseBoolean(map.get("fill").toString()) : false;
                        
                        return new DataSet(label, data, color, fill);
                    })
                    .toList();
        } catch (Exception e) {
            logger.error("解析数据集JSON失败: {}", datasetsJson, e);
            throw new Exception("数据集JSON格式错误: " + e.getMessage());
        }
    }

    /**
     * 辅助方法：生成错误HTML
     */
    private String generateErrorHtml(String errorMessage) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <title>图表生成错误</title>
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        padding: 40px; 
                        background-color: #f8f9fa;
                    }
                    .error-container {
                        max-width: 600px;
                        margin: 0 auto;
                        background: white;
                        padding: 30px;
                        border-radius: 10px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .error-title {
                        color: #dc3545;
                        margin-bottom: 20px;
                    }
                    .error-message {
                        color: #6c757d;
                        margin-bottom: 20px;
                    }
                </style>
            </head>
            <body>
                <div class="error-container">
                    <h1 class="error-title">❌ 图表生成失败</h1>
                    <div class="error-message">%s</div>
                    <p>请检查输入数据格式是否正确。</p>
                </div>
            </body>
            </html>
            """, errorMessage);
    }
}
