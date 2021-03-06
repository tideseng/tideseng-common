package com.tideseng.springcloud;

import com.netflix.client.*;

import org.springframework.cloud.context.named.*;
import org.springframework.cloud.openfeign.*;
import com.netflix.client.config.*;

import org.springframework.beans.factory.support.*;
import org.springframework.cloud.openfeign.loadbalancer.*;
import org.springframework.cloud.openfeign.ribbon.*;
import org.springframework.cloud.openfeign.ribbon.*;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.*;
import org.springframework.core.type.*;
import org.springframework.beans.factory.config.*;

import feign.Feign.*;
import feign.Target.*;
import feign.Request.*;
import feign.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.net.*;

/**
 * Feign与OpenFeign比较
 *      OpenFeign利用了Feign的高扩展性，使用标准的Spring Web MVC来声明客户端接口
 *      Feign
 *          注解扩展性
 *          REST请求元信息解析
 *          HTTP请求处理
 *      OpenFeign
 *          提供Spring Web MVC注解解析
 *          提供Feign自动装配
 * 特点
 *      基于接口注解的方式实现远程通信
 *      屏蔽底层细节
 * 实现
 *      参数的解析和装载
 *      针对指定的FeginClient生成动态代理
 *      针对FeignClient中的方法描述进行解析
 *      组装出一个Request对象，发起请求
 * 注意
 *      Feign容易对特殊字符的参数出现编码解码问题、异步请求获取原请求信息失败问题
 */
public class OpenFeignCommon {

    /**
     * 启动流程
     * 一、BeanDefinition注册/Bean动态装载
     * {@link EnableFeignClients}
     *      {@link EnableFeignClients#basePackages()}扫描@FeignClient包路径
     *      {@link EnableFeignClients#basePackageClasses()}加载FeignClient的全局默认配置
     *      导入{@link FeignClientsRegistrar}（实现了{@link ImportBeanDefinitionRegistrar}接口）
     *          {@link FeignClientsRegistrar#registerBeanDefinitions(AnnotationMetadata, BeanDefinitionRegistry)}方法用于动态注册BeanDefinition
     *              {@link FeignClientsRegistrar#registerDefaultConfiguration(AnnotationMetadata, BeanDefinitionRegistry)}注册@EnableFeignClients注解中的defaultConfiguration默认配置
     *                  {@link StandardAnnotationMetadata#getAnnotationAttributes(String, boolean)}
     *                  {@link FeignClientsRegistrar#registerClientConfiguration(BeanDefinitionRegistry, Object, Object)}
     *              {@link FeignClientsRegistrar#registerFeignClients(AnnotationMetadata, BeanDefinitionRegistry)}注册@FeignClient修饰的类成BeanDefinition
     *                  {@link FeignClientsRegistrar#getBasePackages(AnnotationMetadata)}
     *                  {@link ClassPathScanningCandidateComponentProvider#findCandidateComponents(String)}
     *                  {@link FeignClientsRegistrar#registerClientConfiguration(BeanDefinitionRegistry, Object, Object)}
     *                  {@link FeignClientsRegistrar#registerFeignClient(BeanDefinitionRegistry, AnnotationMetadata, Map)}
     *                      {@link BeanDefinitionBuilder#genericBeanDefinition(Class)}将FeignClient包装成【{@link FeignClientFactoryBean}】的BeanDefinition
     *                      {@link BeanDefinitionReaderUtils#registerBeanDefinition(BeanDefinitionHolder, BeanDefinitionRegistry)}
     * 二、自动装配
     * spring-cloud-openfeign-core依赖包下META-INF/spring.factories文件的EnableAutoConfiguration属性会加载{@link FeignAutoConfiguration}类
     *      初始化{@link FeignContext} >> {@link FeignContext#FeignContext()}
     *          {@link NamedContextFactory#NamedContextFactory(Class, String, String)} 指定{@link FeignClientsConfiguration}动态注入
     *      初始化{@link Targeter} -- {@link HystrixTargeter}
     * spring-cloud-openfeign-core依赖包下META-INF/spring.factories文件的EnableAutoConfiguration属性会加载{@link FeignRibbonClientAutoConfiguration}类
     *      import类{@link HttpClientFeignLoadBalancedConfiguration}、{@link OkHttpFeignLoadBalancedConfiguration}、{@link DefaultFeignLoadBalancedConfiguration}初始化Client
     *          {@link LoadBalancerFeignClient}[【默认情况下OpenFeign的Client为LoadBalancerFeignClient】
     * spring-cloud-openfeign-core依赖包下META-INF/spring.factories文件的EnableAutoConfiguration属性会加载{@link FeignLoadBalancerAutoConfiguration}类
     *      import类{@link HttpClientFeignLoadBalancerConfiguration}、{@link OkHttpFeignLoadBalancerConfiguration}、{@link DefaultFeignLoadBalancerConfiguration}初始化Client
     * 三、FeignClient代理对象生成
     * {@link FeignClientFactoryBean#getObject()} >> {@link FeignClientFactoryBean#getTarget()}生成代理对象
     *      {@link FeignClientFactoryBean#feign(FeignContext)}【通过建造者模式构建Feign.Builder对象，动态配置各个组件】
     *          {@link FeignClientFactoryBean#configureFeign(FeignContext, Builder)}从上下文、默认配置、自定义配置中设置Builder
     *      {@link FeignClientFactoryBean#loadBalance(Builder, FeignContext, HardCodedTarget)}当url为空时
     *          {@link FeignClientFactoryBean#getOptional(FeignContext, Class)} >> {@link NamedContextFactory#getInstance(String, Class)}从上下文(子容器和父容器)中获取{@link LoadBalancerFeignClient}
     *          {@link DefaultTargeter#target(FeignClientFactoryBean, Builder, FeignContext, HardCodedTarget)} >> {@link Builder#target(Target)}【通过动态代理模式生成实际发起请求的代理对象】
     *              {@link Builder#build()}构建Feign的实例ReflectiveFeign
     *              {@link ReflectiveFeign#newInstance(Target)}创建代理对象
     *                  {@link ReflectiveFeign.ParseHandlersByName#apply(Target)}
     *                  构建Map<Method, InvocationHandlerFactory.MethodHandler>结构的methodToHandler
     *                      {@link SpringMvcContract#processAnnotationOnMethod(feign.MethodMetadata, Annotation, Method)}
     *                  {@link InvocationHandlerFactory.Default#create(Target, Map)}构建InvocationHandler
     *                      {@link ReflectiveFeign.FeignInvocationHandler#FeignInvocationHandler(Target, Map)}
     *                  {@link Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler)}动态代理生成对象
     */
    public void bootstrap() {

    }

    /**
     * 调用流程
     * {@link ReflectiveFeign.FeignInvocationHandler#invoke(Object, Method, Object[])}动态代理调用方法
     *      dispatch.get(method)获取method对应的MethodHandler
     *      {@link SynchronousMethodHandler#invoke(Object[])}动态代理对象调用方法
     *          {@link ReflectiveFeign.BuildTemplateByResolvingArgs#create(Object[])}根据参数生成RequestTemplate对象
     *          {@link SynchronousMethodHandler#findOptions(Object[])}根据参数生成Options对象
     *          {@link SynchronousMethodHandler#executeAndDecode(RequestTemplate, Options)}执行请求，先通过RequestTemplate生成Request请求对象，再调用Client对象发起请求获取response响应信息
     *              {@link SynchronousMethodHandler#targetRequest(RequestTemplate)}先执行拦截器链，再通过RequestTemplate生成Request请求对象
     *                  {@link RequestInterceptor#apply(RequestTemplate)}执行feing拦截器
     *                  {@link HardCodedTarget#apply(RequestTemplate)}生成Request请求对象
     *              {@link LoadBalancerFeignClient#execute(Request, Options)}执行请求，默认采用JDK的HttpURLConnection发起远程调用
     *                  {@link FeignLoadBalancer.RibbonRequest#RibbonRequest(Client, Request, URI)}构建RibbonRequest
     *                  {@link LoadBalancerFeignClient#lbClient(String)}获取或生成具有负载均衡的Feign（内部维护了Ribbon的IClientConfig、ILoadBalancer等）
     *                  {@link AbstractLoadBalancerAwareClient#executeWithLoadBalancer(ClientRequest, IClientConfig)}根据负载均衡执行请求
     *                      {@link Client.Default#execute(Request, Options)}Feign默认Client的真正执行逻辑，使用了HttpURLConnection的方式来进行请求，并没有使用对象池技术，所以性能较低，在实际工作中推荐使用Okhttp3作为请求连接池，但要注意的是Okhttp3的连接初始化默认只有5个，需要按需设置
     */
    public void invoke() {

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

    /**
     * 动态刷新
     */
    public void autoRefresh() {

    }

}
