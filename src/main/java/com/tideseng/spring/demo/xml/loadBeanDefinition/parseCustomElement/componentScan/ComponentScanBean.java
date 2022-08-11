package com.tideseng.spring.demo.xml.loadBeanDefinition.parseCustomElement.componentScan;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @author jiahuan
 * @create 2022/8/1
 */
@Data
@Component
public class ComponentScanBean {

    private String className = ComponentScanBean.class.getSimpleName();

}