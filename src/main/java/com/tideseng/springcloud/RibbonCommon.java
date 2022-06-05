package com.tideseng.springcloud;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.web.client.*;
import org.springframework.cloud.client.*;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.context.*;
import org.springframework.context.annotation.*;
import org.springframework.web.client.*;

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

}
