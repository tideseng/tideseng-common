package com.tideseng.spring.demo.xml.invokeBeanFactoryPostProcessors.beanDefinitionRegistry.customAnnotatoinRegistry.postProcessor;

import com.tideseng.spring.demo.xml.invokeBeanFactoryPostProcessors.beanDefinitionRegistry.customAnnotatoinRegistry.annotation.MyComponent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

/**
 * @author jiahuan
 * @create 2022/7/21
 */
@Component
public class CustomAnnoBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // 注册自定义的注解Bean
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
        scanner.addIncludeFilter(new AnnotationTypeFilter(MyComponent.class));
        scanner.scan("com.tideseng.spring.demo.xml.invokeBeanFactoryPostProcessors.beanDefinitionRegistry.customAnnotatoinRegistry.bean");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public int getOrder() {
        return 0;
    }

}
