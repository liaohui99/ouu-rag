package com.example.excelrag.config;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/1/27 15:42
 * @description: TODO
 */
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.tool.ToolProvider;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;

@Component
public class MCPToolsInjector implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired(required = false)
    private Map<String, Object> aiServiceBeans;  // 所有 Spring 管理的 bean

    @Autowired(required = false)
    private ToolProvider mcpToolProvider;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (mcpToolProvider == null) {
            System.err.println("MCP 工具提供者未找到");
            return;
        }

        // 查找所有 @AiService 注解的 bean
        aiServiceBeans.forEach((beanName, bean) -> {
            if (bean.getClass().isAnnotationPresent(AiService.class)) {
                injectMcpTools(bean, beanName);
            }
        });
    }

    private void injectMcpTools(Object bean, String beanName) {
        try {
            // 使用反射获取内部字段并注入
            Object target = getTargetObject(bean);

            // 查找并修改工具字段
            java.lang.reflect.Field toolsField = findField(target.getClass(), "toolProvider");
            if (toolsField != null) {
                toolsField.setAccessible(true);
                ToolProvider currentTools = (ToolProvider) toolsField.get(target);

                System.out.println("✅ 成功为 " + beanName + " 注入 MCP 工具");

                // 验证工具数量
            }
        } catch (Exception e) {
            System.err.println("❌ 注入 MCP 工具到 " + beanName + " 失败: " + e.getMessage());
        }
    }

    // 辅助方法获取代理目标
    private Object getTargetObject(Object proxy) throws Exception {
        if (org.springframework.aop.framework.Advised.class.isAssignableFrom(proxy.getClass())) {
            return ((org.springframework.aop.framework.Advised) proxy).getTargetSource().getTarget();
        }
        return proxy;
    }

    // 查找字段
    private java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}
