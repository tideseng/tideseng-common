package com.tideseng.springcloud;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.eureka.DefaultEurekaServerContext;
import com.netflix.eureka.EurekaServerConfig;
import com.netflix.eureka.EurekaServerContext;
import com.netflix.eureka.cluster.PeerEurekaNodes;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import org.springframework.cloud.netflix.eureka.server.*;
import java.util.*;

import java.lang.annotation.Annotation;

/**
 * Eureka Server端功能：服务启动、服务同步、服务剔除、自我保护
 * Eureka Client端功能：服务启动、服务注册、服务续约、服务下线
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
         *      {@link DefaultEurekaServerContext#initialize()}
         *          {@link PeerEurekaNodes#start()} >> {@link PeerEurekaNodes#updatePeerEurekaNodes(List)}
         *          {@link PeerAwareInstanceRegistry#init(PeerEurekaNodes)}
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
