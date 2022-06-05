package com.tideseng.springcloud;

import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.web.client.*;
import org.springframework.cloud.client.*;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.client.loadbalancer.RestTemplateCustomizer;
import org.springframework.cloud.netflix.ribbon.*;
import org.springframework.context.*;
import org.springframework.context.annotation.*;
import org.springframework.http.client.*;
import org.springframework.http.client.support.*;
import org.springframework.web.client.*;
import org.springframework.http.*;

import com.netflix.loadbalancer.*;

import java.net.*;

/**
 * 技术的驱动基于需求，Ribbon的出现基于分布式架构中的远程通信
 * Ribbon实现负载均衡原理
 *      1.解析配置/缓存中的服务列表地址
 *      2.基于负载均衡算法实现请求分发
 * @author jiahuan
 * @create 2022/06/05
 */
public class RibbonCommon {

    /**
     * 1.基于RestTemplate进行远程通信（RestTemplate本质上是对http请求通信的封装）
     */
    public void restTemplate(RestTemplateBuilder restTemplateBuilder, String url) throws Exception {
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.getForObject(url, String.class);
    }

    /**
     * 2.基于LoadBalancerClient进行远程通信
     * 配置服务提供者地址列表
     *      ServiceProvider.ribbon.listOfServers=\localhost:80,localhost:81
     */
    public void loadBalancerClient() throws Exception {
        ServiceInstance serviceInstance = loadBalancerClient.choose("ServiceProvider");
        String url = String.format("http://%s:%s/%s", serviceInstance.getHost(), serviceInstance.getPort(), "users/1");
        restTemplate(null, url);
    }
    @Autowired
    private LoadBalancerClient loadBalancerClient;

    /**
     * 3.基于@LoadBalanced注解进行远程通信（本质上通过增加拦截器解析请求地址）
     * 一、@LoadBalanced注解（本质上是@Qualifier注解）
     * {@link LoadBalanced}
     * 二、RibbonAutoConfiguration自动装配
     * {@link RibbonAutoConfiguration}
     *      {@link LoadBalancerClient}({@link RibbonLoadBalancerClient})、{@link RibbonApplicationContextInitializer}
     * 三、LoadBalancerAutoConfiguration自动装配
     * {@link LoadBalancerAutoConfiguration}
     *      {@link LoadBalancerAutoConfiguration#restTemplates}属性装配
     *      {@link LoadBalancerRequestFactory}、{@link LoadBalancerInterceptor}、{@link RestTemplateCustomizer}、{@link SmartInitializingSingleton}等Bean的装配
     * 四、请求拦截
     * {@link RestTemplate#getForObject(String, Class, Object...)}
     *      {@link RestTemplate#execute(String, HttpMethod, RequestCallback, ResponseExtractor, Object...)} >> {@link RestTemplate#doExecute(URI, HttpMethod, RequestCallback, ResponseExtractor)}
     *          {@link HttpAccessor#createRequest(URI, HttpMethod)}
     *          {@link AbstractClientHttpRequest#execute()} >> {@link AbstractBufferingClientHttpRequest#executeInternal(HttpHeaders)} >> {@link InterceptingClientHttpRequest#executeInternal(HttpHeaders, byte[])}
     *              {@link InterceptingClientHttpRequest.InterceptingRequestExecution#execute(HttpRequest, byte[])}
     *                  {@link LoadBalancerInterceptor#intercept(HttpRequest, byte[], ClientHttpRequestExecution)}
     *                      {@link RibbonLoadBalancerClient#execute(String, LoadBalancerRequest)} >> {@link RibbonLoadBalancerClient#execute(String, LoadBalancerRequest, Object)}
     *                          {@link RibbonLoadBalancerClient#getLoadBalancer(String)}（详见{@link RibbonCommon#getLoadBalancer(String)}）
     *                          {@link RibbonLoadBalancerClient#getServer(ILoadBalancer, Object)}（详见{@link RibbonCommon#getServer(ILoadBalancer, Object)}）
     *                          {@link RibbonLoadBalancerClient#execute(String, ServiceInstance, LoadBalancerRequest)}（详见{@link RibbonCommon#execute(String, ServiceInstance, LoadBalancerRequest)}）
     */
    public void loadBalancedAnnotation() throws Exception {
        restTemplate.getForObject("http://ServiceProvider/users/1", String.class);
    }
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }
    @Autowired
    private RestTemplate restTemplate;

    /**
     * {@link RibbonLoadBalancerClient#getLoadBalancer(String)}
     */
    public void getLoadBalancer(String serviceId) throws Exception {

    }

    /**
     * {@link RibbonLoadBalancerClient#getServer(ILoadBalancer, Object)}
     */
    public void getServer(ILoadBalancer loadBalancer, Object hint) throws Exception {

    }

    /**
     * {@link RibbonLoadBalancerClient#execute(String, ServiceInstance, LoadBalancerRequest)}
     *      {@link LoadBalancerRequestFactory#createRequest(HttpRequest, byte[], ClientHttpRequestExecution)}
     */
    public void execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest request) throws Exception {

    }

}
