package com.example.excelrag.tools.chart;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/1/27 20:23
 * @description: TODO
 */

import com.example.excelrag.tools.enums.ChartConfig;
import com.example.excelrag.tools.enums.ChartRequest;
import com.example.excelrag.tools.enums.ChartType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 图表HTML生成器
 * 使用Chart.js作为前端渲染引擎
 */
public class ChartHtmlGenerator {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final VelocityEngine velocityEngine;

    public ChartHtmlGenerator() {
        this.velocityEngine = new VelocityEngine();
        velocityEngine.setProperty("resource.loader", "class");
        velocityEngine.setProperty("class.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.init();
    }

    /**
     * 生成完整的HTML页面
     */
    public String generateFullHtml(ChartRequest request) {
        try {
            VelocityContext context = new VelocityContext();

            // 准备数据
            String chartConfigJson = generateChartJsConfig(request);
            String chartType = request.getChartType().name().toLowerCase();
            ChartConfig config = request.getConfig();

            // 设置上下文变量
            context.put("title", config.getTitle());
            context.put("width", config.getWidth());
            context.put("height", config.getHeight());
            context.put("chartType", chartType);
            context.put("chartConfigJson", chartConfigJson);
            context.put("theme", config.getTheme());
            context.put("showLegend", config.isShowLegend());
            context.put("showGrid", config.isShowGrid());
            context.put("interactive", config.isInteractive());

            // 选择模板
            String templateName = "templates/chart-template.vm";
            if (config.getTheme().equals("dark")) {
                templateName = "templates/chart-template-dark.vm";
            }

            // 渲染模板
            try {
                Template template = velocityEngine.getTemplate(templateName);
                StringWriter writer = new StringWriter();
                template.merge(context, writer);
                return writer.toString();
            } catch (Exception e) {
                // 如果模板渲染失败，返回一个简单的HTML
                return generateSimpleHtml(chartConfigJson, config);
            }
        } catch (Exception e) {
            return generateErrorHtml("生成图表失败: " + e.getMessage());
        }
    }

    /**
     * 生成Chart.js配置
     */
    private String generateChartJsConfig(ChartRequest request) {
        Map<String, Object> config = new HashMap<>();

        // 基础配置
        config.put("type", request.getChartType().name().toLowerCase());

        // 数据配置
        Map<String, Object> dataConfig = new HashMap<>();
        dataConfig.put("labels", request.getLabels());

        List<Map<String, Object>> datasets = request.getDatasets().stream()
                .map(dataset -> {
                    Map<String, Object> ds = new HashMap<>();
                    ds.put("label", dataset.getLabel());
                    ds.put("data", dataset.getData());
                    ds.put("backgroundColor", dataset.getColor());
                    ds.put("borderColor", dataset.getColor().replace("0.2", "1"));
                    ds.put("borderWidth", 2);
                    ds.put("fill", dataset.isFill());

                    // 根据图表类型添加特定配置
                    if (request.getChartType() == ChartType.LINE ||
                            request.getChartType() == ChartType.AREA) {
                        ds.put("tension", 0.4);
                        ds.put("pointRadius", 4);
                    }

                    if (request.getChartType() == ChartType.BAR) {
                        ds.put("borderRadius", 4);
                    }

                    return ds;
                })
                .collect(Collectors.toList());

        dataConfig.put("datasets", datasets);
        config.put("data", dataConfig);

        // 选项配置
        Map<String, Object> options = new HashMap<>();
        options.put("responsive", true);
        options.put("maintainAspectRatio", false);

        // 插件配置
        Map<String, Object> plugins = new HashMap<>();

        Map<String, Object> legend = new HashMap<>();
        legend.put("display", request.getConfig().isShowLegend());
        legend.put("position", "top");
        plugins.put("legend", legend);

        Map<String, Object> tooltip = new HashMap<>();
        tooltip.put("enabled", true);
        tooltip.put("mode", "index");
        tooltip.put("intersect", false);
        plugins.put("tooltip", tooltip);

        options.put("plugins", plugins);

        // 坐标轴配置
        if (request.getChartType() != ChartType.PIE &&
                request.getChartType() != ChartType.DONUT) {
            Map<String, Object> scales = new HashMap<>();

            Map<String, Object> xAxis = new HashMap<>();
            xAxis.put("display", true);
            if (request.getConfig().isShowGrid()) {
                Map<String, Object> grid = new HashMap<>();
                grid.put("drawBorder", true);
                grid.put("color", "rgba(0, 0, 0, 0.1)");
                xAxis.put("grid", grid);
            }
            scales.put("x", xAxis);

            Map<String, Object> yAxis = new HashMap<>();
            yAxis.put("display", true);
            if (request.getConfig().isShowGrid()) {
                Map<String, Object> grid = new HashMap<>();
                grid.put("drawBorder", true);
                grid.put("color", "rgba(0, 0, 0, 0.1)");
                yAxis.put("grid", grid);
            }
            scales.put("y", yAxis);

            options.put("scales", scales);
        }

        config.put("options", options);

        // 添加自定义选项
        config.putAll(request.getConfig().getCustomOptions());

        try {
            return OBJECT_MAPPER.writeValueAsString(config);
        } catch (Exception e) {
            return "{}";
        }
    }

    /**
     * 生成简化版HTML（模板渲染失败时使用）
     */
    private String generateSimpleHtml(String chartConfigJson, ChartConfig config) {
        return String.format("""
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <title>%s</title>
                            <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                            <style>
                                body { 
                                    font-family: Arial, sans-serif; 
                                    margin: 0; 
                                    padding: 20px; 
                                    background-color: #f5f5f5;
                                }
                                .chart-container {
                                    width: %dpx;
                                    height: %dpx;
                                    margin: 0 auto;
                                    background: white;
                                    border-radius: 8px;
                                    padding: 20px;
                                    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                                }
                                .chart-title {
                                    text-align: center;
                                    margin-bottom: 20px;
                                    color: #333;
                                    font-size: 24px;
                                }
                            </style>
                        </head>
                        <body>
                            <div class="chart-container">
                                <h1 class="chart-title">%s</h1>
                                <canvas id="chartCanvas"></canvas>
                            </div>
                            
                            <script>
                                const ctx = document.getElementById('chartCanvas').getContext('2d');
                                const config = %s;
                                new Chart(ctx, config);
                            </script>
                        </body>
                        </html>
                        """, config.getTitle(), config.getWidth(), config.getHeight(),
                config.getTitle(), chartConfigJson);
    }

    /**
     * 生成内联HTML片段（用于嵌入到其他页面）
     */
    public String generateInlineHtml(ChartRequest request) {
        String chartConfigJson = generateChartJsConfig(request);

        return String.format("""
                        <div style="width: %dpx; height: %dpx; margin: 20px auto;">
                            <canvas id="chart_%s"></canvas>
                        </div>
                        <script>
                            (function() {
                                const ctx = document.getElementById('chart_%s').getContext('2d');
                                const config = %s;
                                new Chart(ctx, config);
                            })();
                        </script>
                        """,
                request.getConfig().getWidth(),
                request.getConfig().getHeight(),
                UUID.randomUUID().toString().replace("-", ""),
                UUID.randomUUID().toString().replace("-", ""),
                chartConfigJson);
    }

    /**
     * 生成错误HTML
     */
    private String generateErrorHtml(String errorMessage) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>图表生成错误</title>
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        padding: 40px; 
                        background-color: #f8f9fa;
                        margin: 0;
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
                        font-size: 24px;
                    }
                    .error-message {
                        color: #6c757d;
                        margin-bottom: 20px;
                        font-size: 16px;
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
