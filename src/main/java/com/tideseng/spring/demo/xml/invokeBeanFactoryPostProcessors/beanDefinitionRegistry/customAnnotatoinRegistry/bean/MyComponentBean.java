package com.tideseng.spring.demo.xml.invokeBeanFactoryPostProcessors.beanDefinitionRegistry.customAnnotatoinRegistry.bean;

import com.tideseng.spring.demo.xml.invokeBeanFactoryPostProcessors.beanDefinitionRegistry.customAnnotatoinRegistry.annotation.MyComponent;
import lombok.Data;

/**
 * @author jiahuan
 * @create 2022/7/21
 */
@Data
@MyComponent
public class MyComponentBean {

    private String className = MyComponentBean.class.getSimpleName();

}
