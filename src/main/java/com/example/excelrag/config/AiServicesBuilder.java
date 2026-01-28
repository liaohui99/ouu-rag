package com.example.excelrag.config;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/1/27 16:23
 * @description: TODO
 */

@Slf4j
@Configuration
public class AiServicesBuilder {

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Bean
    public List<ToolSpecification> scanForToolMethods() {
        List<ToolSpecification> toolSpecifications = new ArrayList<>();

        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            try {
                String beanClassName = beanFactory.getBeanDefinition(beanName).getBeanClassName();
                if (beanClassName == null) {
                    continue;
                }
                Class<?> beanClass = Class.forName(beanClassName);
                for (Method beanMethod : beanClass.getDeclaredMethods()) {
                    if (beanMethod.isAnnotationPresent(Tool.class)) {
                        //toolBeanNames.add(beanName);
                        try {
                            toolSpecifications.add(ToolSpecifications.toolSpecificationFrom(beanMethod));
                        } catch (Exception e) {
                            log.warn("Cannot convert %s.%s method annotated with @Tool into ToolSpecification"
                                    .formatted(beanClass.getName(), beanMethod.getName()), e);
                        }
                    }
                }
            } catch (Exception e) {
                // TODO
            }
        }

        return toolSpecifications;
    }

}
