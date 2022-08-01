package com.tideseng.spring.demo.xml.loadBeanDefinition.parseDefaultElement.replace;

import org.springframework.beans.factory.support.MethodReplacer;

import java.lang.reflect.Method;

public class V2OrderApi implements MethodReplacer {

    @Override
    public Object reimplement(Object obj, Method method, Object[] args) throws Throwable {
        System.out.println("新版下单方法商品Id: " + args);
        return "20220717-1111";
    }

}
