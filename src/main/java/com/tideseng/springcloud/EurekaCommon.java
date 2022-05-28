package com.tideseng.springcloud;

import com.netflix.appinfo.*;
import com.netflix.discovery.*;
import com.netflix.eureka.*;
import com.netflix.eureka.cluster.*;
import com.netflix.eureka.registry.*;
import org.springframework.cloud.netflix.eureka.server.*;
import javax.servlet.*;
import java.util.*;

import java.lang.annotation.Annotation;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 注册中心产生背景:
 *      服务提供者的上下线需要服务调用者动态感知，减少服务调用地址的维护工作
 * Eureka实现原理:
 *      引入第三方服务注册中心节点，其它服务将地址提交给第三方，使其管理所有的服务地址，服务调用者从第三方获取服务提供者地址列表
 * Eureka功能:
 *      Eureka Server端功能：服务启动、服务同步、服务剔除、自我保护、三级缓存
 *      Eureka Client端功能：服务启动、服务注册、服务续约、服务下线
 * 注册中心差异点:
 *      高可用机制（CAP特性、一致性问题）
 *      API调用方式
 *      存储方式
 *      通知方式
 * @author jiahuan
 * @create 2022/5/26
 */
public class EurekaCommon {

    /**
     * Eureka Server端功能：服务启动、服务同步、服务剔除、自我保护
     */
    class EurekaServer {

        /**
         * 服务启动流程：
         *  1.启动类添加{@link EnableEurekaServer}注解，该注解导入{@link EurekaServerMarkerConfiguration}类，初始化{@link EurekaServerMarkerConfiguration.Marker}内部类
         *      {@link EnableEurekaServer}注解 >> 导入{@link EurekaServerMarkerConfiguration} >> 初始化{@link EurekaServerMarkerConfiguration.Marker}标记类
         *  2.spring-cloud-netflix-eureka-server依赖包下META-INF/spring.factories文件的EnableAutoConfiguration属性会加载{@link EurekaServerAutoConfiguration}类，
         *      spring.factories扩展点 >> 加载{@link EurekaServerAutoConfiguration}
         *          注入{@link ApplicationInfoManager}、{@link EurekaClient}、{@link EurekaServerConfig}、{@link EurekaClientConfig}、{@link InstanceRegistryProperties}
         *          初始化{@link PeerAwareInstanceRegistry}、{@link PeerEurekaNodes}、{@link EurekaServerContext}、{@link EurekaServerBootstrap}
         *          导入{@link EurekaServerInitializerConfiguration}
         * 3.DefaultEurekaServerContext对象初始化完毕后调用@PostConstruct修饰的{@link DefaultEurekaServerContext#initialize()}方法初始化上下文
         *      {@link DefaultEurekaServerContext#initialize()}初始化Eureka Server上下文
         *          {@link PeerEurekaNodes#start()}初始化集群节点列表并开启定时任务 >> {@link PeerEurekaNodes#updatePeerEurekaNodes(List)}根据集群节点地址列表初始化集群节点列表 >> scheduleWithFixedDelay定时每隔10分钟更新集群节点列表
         *          {@link PeerAwareInstanceRegistryImpl#init(PeerEurekaNodes)}初始化ResponseCacheImpl缓存并启动定时器更新自我保护阈值 >> {@link AbstractInstanceRegistry#initializedResponseCache()}初始化本地缓存类 >> {@link PeerAwareInstanceRegistryImpl#scheduleRenewalThresholdUpdateTask()}开启每隔15分钟更新每分钟续约因子阈值
         * 4.EurekaServerInitializerConfiguration基于lifecycle回调start方法初始化Eureka Server并启动
         *      {@link EurekaServerInitializerConfiguration#start()} >> {@link EurekaServerBootstrap#contextInitialized(ServletContext)} >> {@link EurekaServerBootstrap#initEurekaEnvironment()} >> {@link EurekaServerBootstrap#initEurekaServerContext()}
         */
        public void bootstrap() throws Exception {
            // 1.启动类添加@EnableEurekaServer注解，导入EurekaServerMarkerConfiguration，初始化EurekaServerMarkerConfiguration.Marker
            Annotation enableEurekaServer = this.getClass().getAnnotation(EnableEurekaServer.class);
            EurekaServerMarkerConfiguration eurekaServerMarkerConfiguration = new EurekaServerMarkerConfiguration();
            // EurekaServerMarkerConfiguration初始化内部类Marker标记类，使EurekaServerAutoConfiguration条件注解生效
            eurekaServerMarkerConfiguration.eurekaServerMarkerBean();

            // 2.Spring Boot的spring.factories扩展点会加载EurekaServerAutoConfiguration类，属性注入、初始化、导入配置类
            EurekaServerAutoConfiguration eurekaServerAutoConfiguration = new EurekaServerAutoConfiguration();
            // 初始化PeerAwareInstanceRegistry（实际上是InstanceRegistry）、PeerEurekaNodes（实际上是RefreshablePeerEurekaNodes）、EurekaServerContext（实际上是DefaultEurekaServerContext）、EurekaServerBootstrap
            PeerAwareInstanceRegistry peerAwareInstanceRegistry = eurekaServerAutoConfiguration.peerAwareInstanceRegistry(null);
            PeerEurekaNodes peerEurekaNodes = eurekaServerAutoConfiguration.peerEurekaNodes(peerAwareInstanceRegistry, null, null);
            EurekaServerContext eurekaServerContext = eurekaServerAutoConfiguration.eurekaServerContext(null, peerAwareInstanceRegistry, peerEurekaNodes);
            EurekaServerBootstrap eurekaServerBootstrap = eurekaServerAutoConfiguration.eurekaServerBootstrap(peerAwareInstanceRegistry, eurekaServerContext);
            // 导入EurekaServerInitializerConfiguration类
            EurekaServerInitializerConfiguration eurekaServerInitializerConfiguration = new EurekaServerInitializerConfiguration();

            // 3.调用DefaultEurekaServerContext#initialize()方法初始化上下文
            eurekaServerContext.initialize();
            peerEurekaNodes.start();
            peerAwareInstanceRegistry.init(peerEurekaNodes);
        }

    }

}
