package com.tideseng.spring.demo.xml.invokeBeanFactoryPostProcessors.beanDefinitionRegistry.customPlaceholderRegistry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Properties;

@Component
public class PropertyPlaceholderBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        try {
            Properties properties = PropertiesLoaderUtils.loadAllProperties("properties/application.properties", ClassUtils.getDefaultClassLoader());
            String property = properties.getProperty("placeholder.beanClass");
            String[] beanClassArr = StringUtils.commaDelimitedListToStringArray(property);
            for (String beanClass : beanClassArr) {
                BeanDefinition beanDefinition = new GenericBeanDefinition();
                beanDefinition.setBeanClassName(beanClass);
                String beanName = BeanDefinitionReaderUtils.generateBeanName(beanDefinition, registry);
                registry.registerBeanDefinition(beanName, beanDefinition);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

}
