package com.example.excelrag.config;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/1/27 15:40
 * @description: TODO
 */

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.tool.ToolProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;

@Configuration
public class MCPBeanPostProcessor implements BeanPostProcessor {

    @Autowired(required = false)
    private McpToolProvider mcpToolProvider;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 检查是否是有 @AiService 注解的 bean
        if (bean.getClass().isAnnotationPresent(AiService.class)) {
            try {
                // 获取 @AiService 注解
                AiService annotation = bean.getClass().getAnnotation(AiService.class);

                // 获取代理对象的实际实例
                Object target = getProxyTarget(bean);

                if (target != null) {
                    // 获取 AI 服务内部的工具字段
                    Field toolsField = findToolsField(target.getClass());

                    if (toolsField != null && mcpToolProvider != null) {
                        toolsField.setAccessible(true);
                        ToolProvider existingTools = (ToolProvider) toolsField.get(target);

                        System.out.println("成功动态注入 MCP 工具到: " + beanName);
                    }
                }
            } catch (Exception e) {
                System.err.println("动态注入 MCP 工具失败: " + e.getMessage());
            }
        }

        return bean;
    }

    private Object getProxyTarget(Object proxy) {
        try {
            // 对于 Spring AOP 代理
            if (org.springframework.aop.support.AopUtils.isAopProxy(proxy)) {
                return org.springframework.aop.support.AopUtils.getTargetClass(proxy);
            }
            return proxy;
        } catch (Exception e) {
            return null;
        }
    }

    private Field findToolsField(Class<?> clazz) {
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getType().isAssignableFrom(ToolProvider.class)) {
                    return field;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
}
