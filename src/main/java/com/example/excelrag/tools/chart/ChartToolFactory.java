package com.example.excelrag.tools.chart;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/1/27 20:34
 * @description: TODO
 */

import com.example.excelrag.tools.DataVisualizationTool;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;

import java.util.List;

/**
 * 简化的工具工厂，提供静态方法创建工具
 */
public class ChartToolFactory {

    /**
     * 创建数据可视化工具实例
     */
    public static DataVisualizationTool createTool() {
        return new DataVisualizationTool();
    }

    /**
     * 获取工具规格列表
     */
    public static List<ToolSpecification> getToolSpecifications() {
        return ToolSpecifications.toolSpecificationsFrom(createTool());
    }

    /**
     * 创建工具执行器
     */
    public static ChartToolExecutor createExecutor() {
        return new ChartToolExecutor(createTool());
    }
}
