package com.example.excelrag.tools;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/1/27 20:31
 * @description: TODO
 */

import java.util.Map;

/**
 * 工具规格文档（供开发者和Agent理解）
 */
public class ChartToolDocumentation {

    /**
     * 工具名称和描述映射
     */
    public static final Map<String, String> TOOL_DESCRIPTIONS = Map.of(
            "generate_chart",
            """
            通用图表生成工具，支持所有图表类型。
            
            参数:
            - chartType: 图表类型 (LINE, BAR, PIE, DONUT, SCATTER, AREA, RADAR, HEATMAP)
            - labels: X轴标签或类别，可以是逗号分隔的字符串或JSON数组
            - datasetsJson: 数据集JSON字符串，格式示例: 
              [{"label":"系列1","data":[1,2,3],"color":"rgba(255,99,132,0.2)","fill":true}]
            - title: 图表标题
            - width: 图表宽度（像素），可选，默认800
            - height: 图表高度（像素），可选，默认500
            - theme: 主题风格，可选，默认"default"
            
            返回: 完整的HTML页面，包含图表
            """,

            "generate_bar_chart",
            """
            快速生成柱状图，用于比较不同类别的数值。
            
            参数:
            - title: 图表标题
            - categories: 分类标签，逗号分隔
            - values: 数值，逗号分隔的数字
            - color: 柱状图颜色，CSS颜色格式，可选
            
            返回: 完整的HTML页面
            """,

            "generate_pie_chart",
            """
            生成饼图，显示各部分的比例分布。
            
            参数:
            - title: 图表标题
            - labels: 各部分标签，逗号分隔
            - values: 各部分数值，逗号分隔的数字
            - colors: 颜色方案，可选
            
            返回: 完整的HTML页面
            """,

            "generate_line_chart_timeseries",
            """
            生成时间序列折线图，显示数据随时间的变化趋势。
            
            参数:
            - title: 图表标题
            - timeLabels: 时间标签，逗号分隔
            - datasetsJson: 数据集JSON字符串
            - showPoints: 是否显示数据点，可选
            - fillArea: 是否填充区域，可选
            
            返回: 完整的HTML页面
            """,

            "generate_inline_chart",
            """
            生成内联图表HTML片段，可以嵌入到现有网页中。
            
            参数:
            - chartType: 图表类型
            - labels: 标签
            - values: 数值
            - title: 标题
            - width: 宽度
            - height: 高度
            
            返回: HTML片段（不是完整页面）
            """
    );

    /**
     * 图表类型说明
     */
    public static final Map<String, String> CHART_TYPE_DESCRIPTIONS = Map.of(
            "LINE", "折线图：显示数据随时间或其他连续变量的变化趋势",
            "BAR", "柱状图：比较不同类别的数值大小",
            "PIE", "饼图：显示各部分占总体的比例",
            "DONUT", "环形图：类似饼图，中间有空心",
            "SCATTER", "散点图：显示两个变量之间的关系",
            "AREA", "面积图：类似折线图，但填充了颜色区域",
            "RADAR", "雷达图：显示多个维度的数据",
            "HEATMAP", "热力图：用颜色表示数据密度或强度"
    );

    /**
     * 颜色格式示例
     */
    public static final String COLOR_EXAMPLES = """
        颜色格式示例:
        - 单个颜色: "rgba(255, 99, 132, 0.6)"
        - 颜色数组: "['#FF6384', '#36A2EB', '#FFCE56']"
        - 渐变色: "linear-gradient(45deg, #FF6384, #36A2EB)"
        """;
}
