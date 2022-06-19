package com.tideseng.springcloud;

import com.netflix.client.config.*;
import com.netflix.loadbalancer.*;

import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.web.client.*;
import org.springframework.cloud.client.*;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.client.loadbalancer.RestTemplateCustomizer;
import org.springframework.cloud.context.named.*;
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
        String url = String.format("%s://%s:%s/%s", serviceInstance.isSecure() ? "https" : "http", serviceInstance.getHost(), serviceInstance.getPort(), "users/1");
        restTemplate(restTemplateBuilder, url);
    }
    @Autowired
    private LoadBalancerClient loadBalancerClient;
    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    /**
     * 3.基于@LoadBalanced注解进行远程通信（本质上通过增加LoadBalancerInterceptor拦截器解析请求地址）
     * 一、@LoadBalanced注解（本质上是@Qualifier注解）
     * {@link LoadBalanced}
     * 二、RibbonAutoConfiguration自动装配（在spring-cloud-netflix-ribbon包中）
     * {@link RibbonAutoConfiguration}
     *      初始化{@link SpringClientFactory}、{@link LoadBalancerClient}({@link RibbonLoadBalancerClient})、{@link RibbonApplicationContextInitializer}
     *          初始化{@link SpringClientFactory}时会调用父类{@link NamedContextFactory#NamedContextFactory(Class, String, String)}构造函数设置Ribbon客户端的默认配置类{@link RibbonClientConfiguration}
     * 三、LoadBalancerAutoConfiguration自动装配（在spring-cloud-commons包中）
     * {@link LoadBalancerAutoConfiguration}
     *      注入属性{@link LoadBalancerAutoConfiguration#restTemplates}（注入修饰了@LoadBalanced(@Qualifier标记)的RestTemplate）
     *      初始化{@link LoadBalancerRequestFactory}
     *          {@link LoadBalancerRequestFactory#LoadBalancerRequestFactory(LoadBalancerClient, List)}，该类中的createRequest方法是一个函数式接口用于真正执行请求
     *      初始化{@link LoadBalancerInterceptor}
     *          {@link LoadBalancerInterceptor#LoadBalancerInterceptor(LoadBalancerClient, LoadBalancerRequestFactory)}，该类中的intercept方法用于拦截请求
     *      初始化{@link RestTemplateCustomizer}
     *          在{@link LoadBalancerAutoConfiguration.LoadBalancerInterceptorConfig#restTemplateCustomizer(LoadBalancerInterceptor)}中用函数式接口实现，用于将拦截器设置到RestTemplate中
     *      初始化{@link SmartInitializingSingleton}
     *          在{@link LoadBalancerAutoConfiguration#loadBalancedRestTemplateInitializerDeprecated(ObjectProvider)}中用函数式接口实现，遍历用@LoadBalanced注解修饰的RestTemplate实例，调用上述{@link RestTemplateCustomizer}的函数式接口方法
     * 四、请求拦截
     * {@link RestTemplate#getForObject(String, Class, Object...)}发起远程通信请求
     *      {@link RestTemplate#execute(String, HttpMethod, RequestCallback, ResponseExtractor, Object...)} >> {@link RestTemplate#doExecute(URI, HttpMethod, RequestCallback, ResponseExtractor)}执行请求
     *          {@link HttpAccessor#createRequest(URI, HttpMethod)}创建ClientHttpRequest
     *              {@link InterceptingHttpAccessor#getRequestFactory()}获取ClientHttpRequestFactory，用于创建ClientHttpRequest
     *                  {@link InterceptingClientHttpRequestFactory#InterceptingClientHttpRequestFactory(ClientHttpRequestFactory, List)}创建InterceptingClientHttpRequestFactory并注入拦截器
     *              {@link AbstractClientHttpRequestFactoryWrapper#createRequest(URI, HttpMethod)}创建ClientHttpRequest >> {@link InterceptingClientHttpRequestFactory#createRequest(URI, HttpMethod, ClientHttpRequestFactory)} >> {@link InterceptingClientHttpRequest#InterceptingClientHttpRequest(ClientHttpRequestFactory, List, URI, HttpMethod)}
     *          {@link AbstractClientHttpRequest#execute()}执行请求 >> {@link AbstractBufferingClientHttpRequest#executeInternal(HttpHeaders)} >> {@link InterceptingClientHttpRequest#executeInternal(HttpHeaders, byte[])}
     *              {@link InterceptingClientHttpRequest.InterceptingRequestExecution#execute(HttpRequest, byte[])}执行请求
     *                  {@link LoadBalancerInterceptor#intercept(HttpRequest, byte[], ClientHttpRequestExecution)}执行LoadBalancerInterceptor拦截方法（有拦截器的时候）
     *                      {@link LoadBalancerRequestFactory#createRequest(HttpRequest, byte[], ClientHttpRequestExecution)}构建LoadBalancerRequest
     *                      {@link RibbonLoadBalancerClient#execute(String, LoadBalancerRequest)} >> {@link RibbonLoadBalancerClient#execute(String, LoadBalancerRequest, Object)}将拦截器委托给RibbonLoadBalancerClient去调用
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
     * 一、加载RibbonClientConfiguration
     * {@link RibbonLoadBalancerClient#getLoadBalancer(String)}获取负载均衡器
     *      {@link SpringClientFactory#getLoadBalancer(String)} >> {@link SpringClientFactory#getInstance(String, Class)}利用工厂模式，根据serviceId获取ILoadBalancer实例（NamedContextFactory机制，会加载{@link RibbonClientConfiguration}）
     *          {@link NamedContextFactory#getInstance(String, Class)}根据serviceId从容器中获取ILoadBalancer的Bean对象
     *              {@link NamedContextFactory#getContext(String)}根据serviceId获取ApplicationContext
     *                  {@link NamedContextFactory#createContext(String)}当缓存不存在时创建ApplicationContext
     *                      {@link AnnotationConfigApplicationContext#register(Class[])}注册默认配置类{@link RibbonClientConfiguration}（在{@link RibbonAutoConfiguration#springClientFactory()}中进行了设置）
     * 二、初始化{@link RibbonClientConfiguration}中的Bean
     *      初始化{@link IClientConfig}客户端负载均衡器配置
     *          {@link DefaultClientConfigImpl#DefaultClientConfigImpl()}
     *      初始化{@link IRule}负载均衡策略
     *          {@link ZoneAvoidanceRule#ZoneAvoidanceRule()}区域感知轮询负载均衡策略
     *              {@link ZoneAvoidancePredicate#ZoneAvoidancePredicate(IRule)}
     *              {@link AvailabilityPredicate#AvailabilityPredicate(IRule)}
     *              {@link ZoneAvoidanceRule#createCompositePredicate(ZoneAvoidancePredicate, AvailabilityPredicate)}
     *      初始化{@link IPing}负载均衡探活策略
     *          {@link DummyPing}默认的ping为空操作
     *      初始化{@link ServerList}服务列表
     *          {@link ConfigurationBasedServerList}默认从配置文件中获取服务地址列表
     *      初始化{@link ServerListUpdater}服务列表更新策略
     *          {@link PollingServerListUpdater#PollingServerListUpdater(IClientConfig)}
     *      初始化{@link ServerListFilter}服务列表过来策略
     *          {@link ZonePreferenceServerListFilter}
     *      初始化{@link ILoadBalancer}负载均衡器
     *          {@link ZoneAwareLoadBalancer#ZoneAwareLoadBalancer(IClientConfig, IRule, IPing, ServerList, ServerListFilter, ServerListUpdater)}
     * 三、动态更新服务列表
     *      {@link ZoneAwareLoadBalancer#ZoneAwareLoadBalancer(IClientConfig, IRule, IPing, ServerList, ServerListFilter, ServerListUpdater)}初始化ZoneAwareLoadBalancer
     *          {@link DynamicServerListLoadBalancer#DynamicServerListLoadBalancer(IClientConfig, IRule, IPing, ServerList, ServerListFilter, ServerListUpdater)}调用父类构造函数
     *              {@link BaseLoadBalancer#BaseLoadBalancer(IClientConfig, IRule, IPing)}调用父类构造函数
     *                  {@link BaseLoadBalancer#initWithConfig(IClientConfig, IRule, IPing, LoadBalancerStats)}初始化配置
     *                      {@link BaseLoadBalancer#setPingInterval(int)}设置负载均衡探活定时器
     *                          {@link BaseLoadBalancer#setupPingTask()}设置负载均衡探活定时器
     *                              {@link BaseLoadBalancer#canSkipPing()}默认情况下允许跳过
     *                              {@link Timer#schedule(TimerTask, long, long)}设置负载均衡探活定时器，每隔30秒执行一次
     *                                  {@link BaseLoadBalancer.PingTask#run()}探活任务执行逻辑
     *                                      {@link BaseLoadBalancer.Pinger#runPinger()}
     *                      {@link BaseLoadBalancer#setRule(IRule)}
     *                      {@link BaseLoadBalancer#setPing(IPing)}
     *                      {@link BaseLoadBalancer#init()}
     *              {@link DynamicServerListLoadBalancer#restOfInit(IClientConfig)}设置服务列表更新定时器并更新
     *                  {@link DynamicServerListLoadBalancer#enableAndInitLearnNewServersFeature()}通过定时任务默认每隔30秒动态更新服务列表
     *                  {@link DynamicServerListLoadBalancer#updateListOfServers()}首次同步更新服务列表
     *                      {@link ConfigurationBasedServerList#getUpdatedListOfServers()}从本地配置listOfServers中获取服务列表（是调用Eureka/Nacos获取服务列表的入口）
     *                      {@link ZonePreferenceServerListFilter#getFilteredListOfServers(List)}过滤服务列表
     *                      {@link DynamicServerListLoadBalancer#updateAllServerList(List)}更新服务列表
     *                          {@link DynamicServerListLoadBalancer#setServersList(List)}设置服务列表
     *                              {@link BaseLoadBalancer#setServersList(List)}调用父类设置服务列表
     *                                  {@link BaseLoadBalancer#allServerList}设置allServerList所有服务列表（替换操作）
     */
    public ILoadBalancer getLoadBalancer(String serviceId) throws Exception {
        RibbonLoadBalancerClient ribbonLoadBalancerClient = new RibbonLoadBalancerClient(new SpringClientFactory());
        Object object = ribbonLoadBalancerClient.getClass().getDeclaredMethod("getLoadBalancer", String.class).invoke(ribbonLoadBalancerClient, "serviceId");
        RibbonClientConfiguration ribbonClientConfiguration = new RibbonClientConfiguration();
        /**
         * 负载均衡策略有：{@link RandomRule}随机负载均衡、{@link RoundRobinRule}轮询负载均衡、{@link WeightedResponseTimeRule}加权响应时间负载均衡、{@link ZoneAvoidanceRule}区域感知轮询负载均衡等，默认为{@link ZoneAvoidanceRule}
         */
        ribbonClientConfiguration.ribbonRule(null);
        ILoadBalancer loadBalancer = (ILoadBalancer) object;
        return loadBalancer;
    }

    /**
     * 获取服务提供者
     * {@link RibbonLoadBalancerClient#getServer(ILoadBalancer, Object)}默认根据ZoneAvoidanceRule区域感知轮询负载均衡规则获取服务
     *      {@link ZoneAwareLoadBalancer#chooseServer(Object)} 调用ILoadBalancer实现类的chooseServer方法（默认为ZoneAwareLoadBalancer，继承自BaseLoadBalancer，Object为null时设置为"default"）
     *          {@link BaseLoadBalancer#chooseServer(Object)}
     *              {@link PredicateBasedRule#choose(Object)} 调用IRule实现类的choose方法（默认为ZoneAvoidanceRule，继承自PredicateBasedRule）
     *                  {@link AbstractLoadBalancerRule#getLoadBalancer()}获取负载均衡器
     *                  {@link BaseLoadBalancer#getAllServers()}根据负载均衡器获取allServerList所有服务列表（在{@link DynamicServerListLoadBalancer#restOfInit(IClientConfig)}中进行写入）
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
     * {@link RibbonLoadBalancerClient#execute(String, ServiceInstance, LoadBalancerRequest)}拦截器方法中执行请求
     *      {@link LoadBalancerRequest#apply(ServiceInstance)}接口类 >> {@link LoadBalancerRequestFactory#createRequest(HttpRequest, byte[], ClientHttpRequestExecution)}函数式接口实现
     *          {@link ServiceRequestWrapper#ServiceRequestWrapper(HttpRequest, ServiceInstance, LoadBalancerClient)}对请求进行包装
     *          {@link InterceptingClientHttpRequest.InterceptingRequestExecution#execute(HttpRequest, byte[])}执行请求
     *              {@link ServiceRequestWrapper#getURI()}重构url，获取真实的uri地址
     *                  {@link RibbonLoadBalancerClient#reconstructURI(ServiceInstance, URI)}获取真实的uri地址
     *              {@link SimpleClientHttpRequestFactory#createRequest(URI, HttpMethod)}获取ClientHttpRequest
     *                  {@link SimpleClientHttpRequestFactory#openConnection(URL, Proxy)}
     *                  {@link SimpleClientHttpRequestFactory#prepareConnection(HttpURLConnection, String)}
     *                  {@link SimpleBufferingClientHttpRequest#SimpleBufferingClientHttpRequest(HttpURLConnection, boolean)}
     *              {@link AbstractClientHttpRequest#execute()}执行请求
     *                  {@link AbstractBufferingClientHttpRequest#executeInternal(HttpHeaders)} >> {@link SimpleBufferingClientHttpRequest#executeInternal(HttpHeaders, byte[])}
     */
    public void execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest request) throws Exception {
        RibbonLoadBalancerClient ribbonLoadBalancerClient = new RibbonLoadBalancerClient(new SpringClientFactory());
        RibbonLoadBalancerClient.RibbonServer ribbonServer = new RibbonLoadBalancerClient.RibbonServer(null,
                getServer(getLoadBalancer("serviceId"), "default"), false, null);
        ribbonLoadBalancerClient.execute(serviceId, ribbonServer, request);
    }

    public void interceptor() throws Exception {

    }

    public void RetryTemplate() throws Exception {

    }

}
