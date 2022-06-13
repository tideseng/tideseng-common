package com.tideseng.springcloud;

import org.springframework.cloud.openfeign.*;

import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.*;
import org.springframework.core.type.*;

import java.util.*;

/**
 * 特点
 *      基于接口注解的方式实现远程通信
 *      屏蔽底层细节
 * 实现
 * 参数的解析和装载
 * 针对指定的FeginClient生成动态代理
 * 针对FeignClient中的方法描述进行解析
 * 组装出一个Request对象，发起请求
 */
public class OpenFeignCommon {

    /**
     * 启动流程
     * {@link EnableFeignClients}
     *      {@link EnableFeignClients#basePackages()}扫描@FeignClient包路径
     *      导入{@link FeignClientsRegistrar}（实现了{@link ImportBeanDefinitionRegistrar}接口）
     *          {@link FeignClientsRegistrar#registerBeanDefinitions(AnnotationMetadata, BeanDefinitionRegistry)}方法用于动态装载
     *              {@link FeignClientsRegistrar#registerDefaultConfiguration(AnnotationMetadata, BeanDefinitionRegistry)}
     *              {@link FeignClientsRegistrar#registerFeignClients(AnnotationMetadata, BeanDefinitionRegistry)}
     *                  {@link FeignClientsRegistrar#registerFeignClient(BeanDefinitionRegistry, AnnotationMetadata, Map)}
     *                      {@link BeanDefinitionBuilder#genericBeanDefinition(Class)}
     */
    public void bootstrap() {

    }

    /**
     * 优化请求
     * 一、添加依赖
     *      添加feign-okhttp依赖
     * 二、配置属性
     *      feign.okhttp.enabled=true
     *      feign.httpclient.enabled=false
     */
    public void optimize() {

    }

}
