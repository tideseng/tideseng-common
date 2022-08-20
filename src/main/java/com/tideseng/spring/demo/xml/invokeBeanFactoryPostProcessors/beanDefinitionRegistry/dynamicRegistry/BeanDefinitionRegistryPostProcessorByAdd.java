package com.tideseng.spring.demo.xml.invokeBeanFactoryPostProcessors.beanDefinitionRegistry.dynamicRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

/**
 * @author jiahuan
 * @create 2022/7/21
 */
@Component
public class BeanDefinitionRegistryPostProcessorByAdd implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

    @SneakyThrows
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // 查询BeanDefinition
        String[] beanDefinitionNames = registry.getBeanDefinitionNames();
        System.out.println(new ObjectMapper().writeValueAsString(beanDefinitionNames));

        // 注册指定的BeanDefinition
        GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
        genericBeanDefinition.setBeanClass(DynamicRegistryBean.class);
        MutablePropertyValues propertyValues = genericBeanDefinition.getPropertyValues();
        propertyValues.add("name", "章佳欢");
        String beanName = AnnotationBeanNameGenerator.INSTANCE.generateBeanName(genericBeanDefinition, registry);
        registry.registerBeanDefinition(beanName, genericBeanDefinition);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public int getOrder() {
        return 0;
    }

}
