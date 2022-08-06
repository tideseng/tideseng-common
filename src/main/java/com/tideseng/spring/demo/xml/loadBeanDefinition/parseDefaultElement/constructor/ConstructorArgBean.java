package com.tideseng.spring.demo.xml.loadBeanDefinition.parseDefaultElement.constructor;

import lombok.Data;

@Data
public class ConstructorArgBean {

    private String username;

    private String password;

    public ConstructorArgBean(String username, String password) {
        this.username = username;
        this.password = password;
    }

}
