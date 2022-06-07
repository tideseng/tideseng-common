package com.tideseng.springcloud;

import com.netflix.client.config.*;
import com.netflix.loadbalancer.*;

import com.netflix.servo.monitor.BasicCounter;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.web.client.*;
import org.springframework.cloud.client.*;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.client.loadbalancer.RestTemplateCustomizer;
import org.springframework.cloud.context.named.NamedContextFactory;
import org.springframework.cloud.netflix.ribbon.*;
import org.springframework.context.*;
import org.springframework.context.annotation.*;
import org.springframework.http.client.*;
import org.springframework.http.client.support.*;
import org.springframework.web.client.*;
import org.springframework.http.*;

import java.net.*;
import java.util.*;

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
     *      初始化{@link SpringClientFactory}、{@link LoadBalancerClient}({@link RibbonLoadBalancerClient})、{@link RibbonApplicationContextInitializer}
     *          初始化{@link SpringClientFactory}会调用父类{@link NamedContextFactory#NamedContextFactory(Class, String, String)}构造函数设置默认配置类{@link RibbonClientConfiguration}
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
     *                  {@link LoadBalancerInterceptor#intercept(HttpRequest, byte[], ClientHttpRequestExecution)}执行LoadBalancer拦截器
     *                      {@link LoadBalancerRequestFactory#createRequest(HttpRequest, byte[], ClientHttpRequestExecution)}构建LoadBalancerRequest
     *                      {@link RibbonLoadBalancerClient#execute(String, LoadBalancerRequest)} >> {@link RibbonLoadBalancerClient#execute(String, LoadBalancerRequest, Object)}
     *                          {@link RibbonLoadBalancerClient#getLoadBalancer(String)}获取负载均衡器（详见{@link RibbonCommon#getLoadBalancer(String)}）
     *                          {@link RibbonLoadBalancerClient#getServer(ILoadBalancer, Object)}获取服务提供者（详见{@link RibbonCommon#getServer(ILoadBalancer, Object)}）
     *                          {@link RibbonLoadBalancerClient#execute(String, ServiceInstance, LoadBalancerRequest)}执行请求（详见{@link RibbonCommon#execute(String, ServiceInstance, LoadBalancerRequest)}）
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
     * 获取负载均衡器
     * {@link RibbonLoadBalancerClient#getLoadBalancer(String)}
     *      {@link SpringClientFactory#getLoadBalancer(String)} >> {@link SpringClientFactory#getInstance(String, Class)}利用工厂模式，根据serviceId获取ILoadBalancer实例（NamedContextFactory机制，会加载{@link RibbonClientConfiguration}）
     *          {@link NamedContextFactory#getInstance(String, Class)}根据serviceId从容器中获取ILoadBalancer的Bean对象
     *              {@link NamedContextFactory#getContext(String)}根据serviceId获取ApplicationContext
     *                  {@link NamedContextFactory#createContext(String)}当缓存不存在是创建ApplicationContext
     *                      {@link AnnotationConfigApplicationContext#register(Class[])}注册默认配置类{@link RibbonClientConfiguration}（在{@link RibbonAutoConfiguration#springClientFactory()}中进行了设置）
     *                          {@link RibbonClientConfiguration}初始化{@link IClientConfig}客户端负载均衡器配置、{@link IRule}负载均衡策略、{@link IPing}负载均衡探活策略、{@link ServerList}服务列表、{@link ServerListUpdater}服务列表更新策略、{@link ServerListFilter}服务列表过来策略、{@link ILoadBalancer}负载均衡器
     */
    public ILoadBalancer getLoadBalancer(String serviceId) throws Exception {
        RibbonLoadBalancerClient ribbonLoadBalancerClient = new RibbonLoadBalancerClient(new SpringClientFactory());
        Object object = ribbonLoadBalancerClient.getClass().getDeclaredMethod("getLoadBalancer", String.class).invoke(ribbonLoadBalancerClient, "serviceId");
        RibbonClientConfiguration ribbonClientConfiguration = new RibbonClientConfiguration();
        ILoadBalancer loadBalancer = (ILoadBalancer) object;
        return loadBalancer;
    }

    /**
     * 获取服务提供者
     * {@link RibbonLoadBalancerClient#getServer(ILoadBalancer, Object)}
     *      {@link ZoneAwareLoadBalancer#chooseServer(Object)} 调用ILoadBalancer实现类的chooseServer方法（默认为ZoneAwareLoadBalancer，继承自BaseLoadBalancer，Object为null时设置为"default"）
     *          {@link BaseLoadBalancer#chooseServer(Object)}
     *              {@link BasicCounter#increment()}计数
     *              {@link PredicateBasedRule#choose(Object)} 调用IRule实现类的choose方法（默认为ZoneAvoidanceRule，继承自PredicateBasedRule）
     *                  {@link AbstractLoadBalancerRule#getLoadBalancer()}获取负载均衡器
     *                  {@link BaseLoadBalancer#getAllServers()}根据负载均衡器获取所有服务列表
     *                  {@link AbstractServerPredicate#chooseRoundRobinAfterFiltering(List, Object)}在过滤后的服务列表中进行轮询
     *                      {@link AbstractServerPredicate#getEligibleServers(List, Object)}根据Object过滤出符合条件的服务列表
     *                      {@link AbstractServerPredicate#incrementAndGetModulo(int)}轮询算法
     */
    public Server getServer(ILoadBalancer loadBalancer, Object hint) throws Exception {
        // 下列对象都在RibbonClientConfiguration中进行创建
        DefaultClientConfigImpl config = new DefaultClientConfigImpl();
        ZoneAvoidanceRule rule = new ZoneAvoidanceRule();
        rule.initWithNiwsConfig(config);
        DummyPing dummyPing = new DummyPing();
        ConfigurationBasedServerList serverList = new ConfigurationBasedServerList();
        serverList.initWithNiwsConfig(config);
        ZonePreferenceServerListFilter filter = new ZonePreferenceServerListFilter();
        filter.initWithNiwsConfig(config);
        PollingServerListUpdater pollingServerListUpdater = new PollingServerListUpdater(config);
        loadBalancer = new ZoneAwareLoadBalancer(config, rule, dummyPing, serverList, filter, pollingServerListUpdater);
        return loadBalancer.chooseServer(hint);
    }

    /**
     * 执行请求
     * {@link RibbonLoadBalancerClient#execute(String, ServiceInstance, LoadBalancerRequest)}
     *      {@link LoadBalancerRequest#apply(ServiceInstance)}接口类 >> {@link LoadBalancerRequestFactory#createRequest(HttpRequest, byte[], ClientHttpRequestExecution)}匿名内部类实现
     *          {@link ServiceRequestWrapper#ServiceRequestWrapper(HttpRequest, ServiceInstance, LoadBalancerClient)}
     *          {@link InterceptingClientHttpRequest.InterceptingRequestExecution#execute(HttpRequest, byte[])}
     *              {@link SimpleClientHttpRequestFactory#createRequest(URI, HttpMethod)}
     *                  {@link SimpleClientHttpRequestFactory#openConnection(URL, Proxy)}
     *                  {@link SimpleClientHttpRequestFactory#prepareConnection(HttpURLConnection, String)}
     *                  {@link SimpleBufferingClientHttpRequest#SimpleBufferingClientHttpRequest(HttpURLConnection, boolean)}
     *              {@link AbstractClientHttpRequest#execute()}
     *                  {@link AbstractBufferingClientHttpRequest#executeInternal(HttpHeaders)} >> {@link SimpleBufferingClientHttpRequest#executeInternal(HttpHeaders, byte[])}
     */
    public void execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest request) throws Exception {
        RibbonLoadBalancerClient ribbonLoadBalancerClient = new RibbonLoadBalancerClient(new SpringClientFactory());
        RibbonLoadBalancerClient.RibbonServer ribbonServer = new RibbonLoadBalancerClient.RibbonServer(null,
                getServer(getLoadBalancer("serviceId"), "default"), false, null);
        ribbonLoadBalancerClient.execute(serviceId, ribbonServer, request);
    }

}
