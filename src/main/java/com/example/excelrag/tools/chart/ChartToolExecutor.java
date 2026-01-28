package com.example.excelrag.tools.chart;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/1/27 20:34
 * @description: TODO
 */

import com.example.excelrag.tools.DataVisualizationTool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.service.tool.ToolExecutor;

/**
 * 工具执行器包装类，实现 LangChain4j 的 ToolExecutor 接口
 */
public class ChartToolExecutor implements ToolExecutor {

    private final DataVisualizationTool chartTool;

    public ChartToolExecutor(DataVisualizationTool chartTool) {
        this.chartTool = chartTool;
    }


    public static ChartToolExecutor from(DataVisualizationTool chartTool) {
        return new ChartToolExecutor(chartTool);
    }

    @Override
    public String execute(ToolExecutionRequest toolExecutionRequest, Object memoryId) {
        return chartTool.executeTool(toolExecutionRequest);
    }
}
