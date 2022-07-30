package com.tideseng.spring.demo.xml.loadBeanDefinition.parseDefaultElement.bean;

import lombok.Data;

/**
 * @author tideseng
 */
@Data
public class DefaultBean {

    private String username = DefaultBean.class.getSimpleName();

}
