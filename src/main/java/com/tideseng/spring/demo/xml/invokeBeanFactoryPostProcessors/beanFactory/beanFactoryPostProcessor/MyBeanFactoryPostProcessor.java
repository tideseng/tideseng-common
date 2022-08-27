package com.tideseng.spring.demo.xml.invokeBeanFactoryPostProcessors.beanFactory.beanFactoryPostProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * @author jiahuan
 * @create 2022/7/22
 */
@Component
public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @SneakyThrows
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        String[] beanDefinitionNames = registry.getBeanDefinitionNames();
        System.out.println("MyBeanFactoryPostProcessor: " + new ObjectMapper().writeValueAsString(beanDefinitionNames));

        DefaultListableBeanFactory currBeanFactory = (DefaultListableBeanFactory) beanFactory;
        currBeanFactory.setAllowBeanDefinitionOverriding(true);
        currBeanFactory.setAllowCircularReferences(true);
        currBeanFactory.setAllowRawInjectionDespiteWrapping(true);
    }

}
