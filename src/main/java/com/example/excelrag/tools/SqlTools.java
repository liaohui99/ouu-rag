package com.example.excelrag.tools;

import com.alibaba.fastjson.JSON;
import com.example.excelrag.mapper.CustomExecuteSqlMapper;
import com.example.excelrag.service.ChartDisplayAssistant;
import com.example.excelrag.service.ChatDemoAssistant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.tool.ToolProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/1/27 13:35
 * @description: TODO
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SqlTools {

    private final ChartDisplayAssistant chartDisplayAssistant;
    private final EmbeddingModel embeddingModel;
    private final ChatMemoryProvider chatMemoryProvider;
    private final CustomExecuteSqlMapper sqlMapper;
    private final List<McpClient> mcpClients;
    private final ToolProvider mcpToolProvider;

    @Tool(name = "showDatabase", value = "查看相关的数据库")
    public String showDatabase() {
        return """
                CREATE TABLE `account_composition`  (
                  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                  `stat_date` date NULL DEFAULT NULL COMMENT '统计日期',
                  `account_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '账号名称',
                  `account_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '账号类型',
                  `operation_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '自营/带货',
                  `douyin_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '抖音号',
                  `user_payment_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '用户支付金额',
                  `buyer_count` int(11) NULL DEFAULT 0 COMMENT '成交人数',
                  `avg_order_value` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '客单价',
                  `click_to_order_conversion_rate` decimal(5, 4) NULL DEFAULT 0.0000 COMMENT '商品点击-成交转化率(次数)',
                  `live_payment_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '直播用户支付金额',
                  `short_video_payment_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '短视频用户支付金额',
                  `product_card_payment_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '商品卡用户支付金额',
                  `other_payment_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '其他用户支付金额',
                  `refund_amount_payment_time` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '退款金额(支付时间)',
                  `refund_rate` decimal(5, 4) NULL DEFAULT 0.0000 COMMENT '退款率(支付时间)',
                  PRIMARY KEY (`id`) USING BTREE,
                  INDEX `idx_date`(`stat_date`) USING BTREE,
                  INDEX `idx_douyin_id`(`douyin_id`) USING BTREE,
                  INDEX `idx_account_type`(`account_type`) USING BTREE
                ) ENGINE = InnoDB AUTO_INCREMENT = 173 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '账号构成分析表' ROW_FORMAT = Dynamic;
                                
                                
                                
                         CREATE TABLE `carrier_composition`  (
                           `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                           `stat_date` date NULL DEFAULT NULL COMMENT '统计日期',
                           `operation_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '自营/带货',
                           `carrier_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '载体类型',
                           `account_channel` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '账号/渠道',
                           `douyin_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '抖音号',
                           `user_payment_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '用户支付金额',
                           `order_count` int(11) NULL DEFAULT 0 COMMENT '成交订单数',
                           `buyer_count` int(11) NULL DEFAULT 0 COMMENT '成交人数',
                           `product_click_count` int(11) NULL DEFAULT 0 COMMENT '商品点击次数',
                           `click_to_order_conversion_rate` decimal(5, 4) NULL DEFAULT 0.0000 COMMENT '商品点击-成交转化率(次数)',
                           `avg_order_value` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '客单价',
                           `refund_amount_payment_time` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '退款金额(支付时间)',
                           `refund_rate` decimal(5, 4) NULL DEFAULT 0.0000 COMMENT '退款率(支付时间)',
                           PRIMARY KEY (`id`) USING BTREE,
                           INDEX `idx_date`(`stat_date`) USING BTREE,
                           INDEX `idx_carrier_type`(`carrier_type`) USING BTREE,
                           INDEX `idx_operation_type`(`operation_type`) USING BTREE
                         ) ENGINE = InnoDB AUTO_INCREMENT = 214 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '载体构成分析表' ROW_FORMAT = Dynamic;       
                                
                                
                                
                             CREATE TABLE `cooperation_transaction`  (
                               `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                               `stat_date` date NOT NULL COMMENT '统计日期',
                               `user_payment_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '用户支付金额',
                               `refund_user_payment_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '退款后用户支付金额(支付时间)',
                               `refund_smart_coupon_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '退款后智能优惠券金额(支付时间)',
                               `refund_platform_subsidy_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '退款后电商平台补贴金额(支付时间)',
                               `smart_coupon_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '智能优惠券金额',
                               `platform_subsidy_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '电商平台补贴金额',
                               `order_count` int(11) NULL DEFAULT 0 COMMENT '成交订单数',
                               `buyer_count` int(11) NULL DEFAULT 0 COMMENT '成交人数',
                               `avg_order_value` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '客单价',
                               `payment_per_1000_exposure` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '千次曝光用户支付金额',
                               `refund_amount_refund_time` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '退款金额(退款时间)',
                               `refund_amount_payment_time` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '退款金额(支付时间)',
                               `refund_rate` decimal(5, 4) NULL DEFAULT 0.0000 COMMENT '退款率(支付时间)',
                               `refund_order_count_refund_time` int(11) NULL DEFAULT 0 COMMENT '退款订单数(退款时间)',
                               `product_exposure_users` int(11) NULL DEFAULT 0 COMMENT '商品曝光人数',
                               `product_click_users` int(11) NULL DEFAULT 0 COMMENT '商品点击人数',
                               `product_exposure_count` int(11) NULL DEFAULT 0 COMMENT '商品曝光次数',
                               `product_click_count` int(11) NULL DEFAULT 0 COMMENT '商品点击次数',
                               `product_click_rate` decimal(5, 4) NULL DEFAULT 0.0000 COMMENT '商品点击率(次数)',
                               `click_to_order_conversion_rate` decimal(5, 4) NULL DEFAULT 0.0000 COMMENT '商品点击-成交转化率(次数)',
                               `presale_deposit_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '预售定金',
                               `influencer_subsidy_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '达人补贴金额',
                               `refund_order_count_payment_time` int(11) NULL DEFAULT 0 COMMENT '退款订单数(支付时间)',
                               `completed_refund_amount_payment_time` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '成交退款金额(支付时间)',
                               `completed_refund_amount_refund_time` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '成交退款金额(退款时间)',
                               PRIMARY KEY (`id`) USING BTREE,
                               UNIQUE INDEX `stat_date`(`stat_date`) USING BTREE,
                               INDEX `idx_date`(`stat_date`) USING BTREE
                             ) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '合作成交详情表' ROW_FORMAT = Dynamic;   
                                
                                
                                CREATE TABLE `product_composition`  (
                                  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                                  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品名称',
                                  `product_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品编号',
                                  `carrier_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '载体类型',
                                  `user_payment_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '用户支付金额',
                                  `avg_order_value` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '成交笔单价',
                                  `click_to_order_conversion_rate` decimal(5, 4) NULL DEFAULT 0.0000 COMMENT '商品点击-成交转化率(次数)',
                                  `refund_amount_payment_time` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '退款金额(支付时间)',
                                  `refund_rate` decimal(5, 4) NULL DEFAULT 0.0000 COMMENT '退款率(支付时间)',
                                  `smart_coupon_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '智能优惠券金额',
                                  `platform_subsidy_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '电商平台补贴金额',
                                  `refund_smart_coupon_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '退款后智能优惠券金额(支付时间)',
                                  PRIMARY KEY (`id`) USING BTREE,
                                  INDEX `idx_product_code`(`product_code`) USING BTREE,
                                  INDEX `idx_carrier_type`(`carrier_type`) USING BTREE
                                ) ENGINE = InnoDB AUTO_INCREMENT = 79 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品构成分析表' ROW_FORMAT = Dynamic;
                                
                                CREATE TABLE `self_operated_transaction`  (
                                  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                                  `stat_date` date NOT NULL COMMENT '统计日期',
                                  `user_payment_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '用户支付金额',
                                  `refund_user_payment_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '退款后用户支付金额(支付时间)',
                                  `refund_smart_coupon_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '退款后智能优惠券金额(支付时间)',
                                  `refund_platform_subsidy_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '退款后电商平台补贴金额(支付时间)',
                                  `smart_coupon_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '智能优惠券金额',
                                  `platform_subsidy_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '电商平台补贴金额',
                                  `order_count` int(11) NULL DEFAULT 0 COMMENT '成交订单数',
                                  `buyer_count` int(11) NULL DEFAULT 0 COMMENT '成交人数',
                                  `avg_order_value` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '客单价',
                                  `payment_per_1000_exposure` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '千次曝光用户支付金额',
                                  `refund_amount_refund_time` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '退款金额(退款时间)',
                                  `refund_amount_payment_time` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '退款金额(支付时间)',
                                  `refund_rate` decimal(5, 4) NULL DEFAULT 0.0000 COMMENT '退款率(支付时间)',
                                  `refund_order_count_refund_time` int(11) NULL DEFAULT 0 COMMENT '退款订单数(退款时间)',
                                  `product_exposure_users` int(11) NULL DEFAULT 0 COMMENT '商品曝光人数',
                                  `product_click_users` int(11) NULL DEFAULT 0 COMMENT '商品点击人数',
                                  `product_exposure_count` int(11) NULL DEFAULT 0 COMMENT '商品曝光次数',
                                  `product_click_count` int(11) NULL DEFAULT 0 COMMENT '商品点击次数',
                                  `product_click_rate` decimal(5, 4) NULL DEFAULT 0.0000 COMMENT '商品点击率(次数)',
                                  `click_to_order_conversion_rate` decimal(5, 4) NULL DEFAULT 0.0000 COMMENT '商品点击-成交转化率(次数)',
                                  `presale_deposit_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '预售定金',
                                  `influencer_subsidy_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '达人补贴金额',
                                  `refund_order_count_payment_time` int(11) NULL DEFAULT 0 COMMENT '退款订单数(支付时间)',
                                  `completed_refund_amount_payment_time` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '成交退款金额(支付时间)',
                                  `completed_refund_amount_refund_time` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '成交退款金额(退款时间)',
                                  PRIMARY KEY (`id`) USING BTREE,
                                  UNIQUE INDEX `stat_date`(`stat_date`) USING BTREE,
                                  INDEX `idx_date`(`stat_date`) USING BTREE
                                ) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '自营成交详情表' ROW_FORMAT = Dynamic;
                                
                                
                                CREATE TABLE `transaction_overview`  (
                                  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                                  `stat_date` date NOT NULL COMMENT '统计日期',
                                  `user_payment_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '用户支付金额',
                                  `refund_user_payment_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '退款后用户支付金额(支付时间)',
                                  `refund_smart_coupon_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '退款后智能优惠券金额(支付时间)',
                                  `refund_platform_subsidy_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '退款后电商平台补贴金额(支付时间)',
                                  `smart_coupon_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '智能优惠券金额',
                                  `platform_subsidy_amount` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '电商平台补贴金额',
                                  `order_count` int(11) NULL DEFAULT 0 COMMENT '成交订单数',
                                  `buyer_count` int(11) NULL DEFAULT 0 COMMENT '成交人数',
                                  `avg_order_value` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '客单价',
                                  `payment_per_1000_exposure` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '千次曝光用户支付金额',
                                  `refund_amount_refund_time` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '退款金额(退款时间)',
                                  `refund_amount_payment_time` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '退款金额(支付时间)',
                                  `refund_rate` decimal(5, 4) NULL DEFAULT 0.0000 COMMENT '退款率(支付时间)',
                                  `refund_order_count_refund_time` int(11) NULL DEFAULT 0 COMMENT '退款订单数(退款时间)',
                                  `product_exposure_users` int(11) NULL DEFAULT 0 COMMENT '商品曝光人数',
                                  `product_click_users` int(11) NULL DEFAULT 0 COMMENT '商品点击人数',
                                  `product_exposure_count` int(11) NULL DEFAULT 0 COMMENT '商品曝光次数',
                                  `product_click_count` int(11) NULL DEFAULT 0 COMMENT '商品点击次数',
                                  `product_click_rate` decimal(5, 4) NULL DEFAULT 0.0000 COMMENT '商品点击率(次数)',
                                  `click_to_order_conversion_rate` decimal(5, 4) NULL DEFAULT 0.0000 COMMENT '商品点击-成交转化率(次数)',
                                  `presale_deposit_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '预售定金',
                                  `influencer_subsidy_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '达人补贴金额',
                                  `refund_order_count_payment_time` int(11) NULL DEFAULT 0 COMMENT '退款订单数(支付时间)',
                                  `completed_refund_amount_payment_time` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '成交退款金额(支付时间)',
                                  `completed_refund_amount_refund_time` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '成交退款金额(退款时间)',
                                  PRIMARY KEY (`id`) USING BTREE,
                                  UNIQUE INDEX `stat_date`(`stat_date`) USING BTREE,
                                  INDEX `idx_date`(`stat_date`) USING BTREE,
                                  INDEX `idx_payment_amount`(`user_payment_amount`) USING BTREE
                                ) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '成交概览总表' ROW_FORMAT = Dynamic;
                                
                                """;
    }

    //@Tool(name = "verifySQL", value = "执行sql前校验sql是否准确")
    public void verifySQL(@P("需要执行的sql") String sql,
                          @P("用户的原始问题") String userQuestion,
                          @P("相关的表结构") String tables
    ) {

    }


    @Tool(name = "execSql", value = "执行sql")
    public String execSql(@P("需要执行的sql") String sql) {
        log.info("execSql:{}", sql);
        return JSON.toJSONString(sqlMapper.execute(sql));
    }

    //@Tool(name = "mcpListTools", value = "相关图表美化时,掉用接口，查询并返回相关工具类")
    public List<ToolSpecification> mcpListTools() {
        return mcpClients.get(0).listTools();
    }


/*    @Tool(name = "mcpChartTools", value = "需要使用图表美化时，调用远程的mcp工具")
    public String mcpChartTools(@ToolMemoryId long toolMemoryId) {
        log.info("exec mcpChartTools:{}", toolMemoryId);
        List<ChatMessage> messages = chatMemoryProvider.get(toolMemoryId).messages();
        return chartDisplayAssistant.chat(toolMemoryId,JSON.toJSONString(messages),"调用工具生成相关图表");
    }*/



}
