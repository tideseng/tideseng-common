package com.tideseng.springcloud;

import com.netflix.appinfo.*;
import com.netflix.appinfo.InstanceInfo.*;
import com.netflix.discovery.*;
import com.netflix.discovery.shared.*;
import com.netflix.discovery.shared.resolver.*;
import com.netflix.discovery.shared.transport.jersey.*;
import com.netflix.eureka.*;
import com.netflix.eureka.cluster.*;
import com.netflix.eureka.lease.Lease;
import com.netflix.eureka.registry.*;
import com.netflix.eureka.registry.Key.*;
import com.netflix.eureka.resources.*;
import com.netflix.eureka.util.*;
import com.netflix.loadbalancer.*;
import com.netflix.niws.loadbalancer.*;

import org.springframework.cloud.netflix.eureka.*;
import org.springframework.cloud.netflix.eureka.server.*;
import org.springframework.cloud.netflix.eureka.server.InstanceRegistry;
import org.springframework.cloud.netflix.eureka.serviceregistry.*;
import org.springframework.context.support.*;
import org.springframework.context.*;

import java.lang.annotation.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import javax.inject.*;
import javax.servlet.*;
import javax.ws.rs.core.*;

/**
 * 注册中心产生背景:
 *      服务提供者的上下线需要服务调用者动态感知，减少服务调用地址的维护工作
 * Eureka实现原理:
 *      引入第三方服务注册中心节点，其它服务将地址提交给第三方，使其管理所有的服务地址，服务调用者从第三方获取服务提供者地址列表
 * Eureka功能:
 *      Eureka Server端功能：服务启动、服务同步、服务剔除、自我保护、三级缓存
 *      Eureka Client端功能：服务启动、服务注册、服务续约、服务发现、服务下线
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
         *      {@link DefaultEurekaServerContext#initialize()}初始化Eureka Server上下文（初始化Eureka Server集群节点列表并开启定时更新任务、初始化Eureka Server节点的注册信息缓存器、开启续约阈值定时器）
         *          {@link PeerEurekaNodes#start()}初始化集群节点列表并开启每隔10分钟的定时任务
         *              {@link PeerEurekaNodes#updatePeerEurekaNodes(List)}根据集群节点地址列表初始化集群节点列表 >> scheduleWithFixedDelay定时每隔10分钟更新集群节点列表
         *          {@link PeerAwareInstanceRegistryImpl#init(PeerEurekaNodes)}初始化ResponseCacheImpl缓存并启动定时器更新自我保护阈值
         *              {@link AbstractInstanceRegistry#initializedResponseCache()}初始化本地缓存类 >> {@link PeerAwareInstanceRegistryImpl#scheduleRenewalThresholdUpdateTask()}开启每隔15分钟更新每分钟续约因子阈值
         * 4.EurekaServerInitializerConfiguration基于lifecycle回调start方法启动Eureka Server
         *      {@link EurekaServerInitializerConfiguration#start()}异步启动Eureka Server >> {@link EurekaServerBootstrap#contextInitialized(ServletContext)}启动Eureka Server
         *          {@link EurekaServerBootstrap#initEurekaEnvironment()}初始化Eureka的环境变量
         *          {@link EurekaServerBootstrap#initEurekaServerContext()}启动Eureka Server
         *              {@link PeerAwareInstanceRegistryImpl#syncUp()}从相邻的Eureka Server节点复制注册表
         *              {@link InstanceRegistry#openForTraffic(ApplicationInfoManager, int)}表示可以开始接收请求 >> {@link PeerAwareInstanceRegistryImpl#openForTraffic(ApplicationInfoManager, int)}
         *                  {@link AbstractInstanceRegistry#expectedNumberOfClientsSendingRenews}修改expectedNumberOfClientsSendingRenews值 >> {@link AbstractInstanceRegistry#updateRenewsPerMinThreshold()}更新每分钟续约因子阈值 >> {@link ApplicationInfoManager#setInstanceStatus(InstanceStatus)}设置Eureka节点状态为UP >>
         *                  {@link AbstractInstanceRegistry#postInit()}开启每隔60秒的剔除定时任务
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
            // EurekaServerBootstrap几乎完全复制了原生EurekaBootstrap的代码，因为原生的Eureka是在servlet应用，但是Spring Cloud的应用是运行在内嵌的Tomcat等WEB服务器里面的，这里就是使用EurekaServerBootstrap来做替换，最终是Eureka能够在springboot中使用
            EurekaServerBootstrap eurekaServerBootstrap = eurekaServerAutoConfiguration.eurekaServerBootstrap(peerAwareInstanceRegistry, eurekaServerContext);
            // 导入EurekaServerInitializerConfiguration类
            EurekaServerInitializerConfiguration eurekaServerInitializerConfiguration = new EurekaServerInitializerConfiguration();

            // 3.调用DefaultEurekaServerContext#initialize()方法初始化上下文
            eurekaServerContext.initialize();
            peerEurekaNodes.start();
            peerAwareInstanceRegistry.init(peerEurekaNodes);

            // 4.EurekaServerInitializerConfiguration基于lifecycle回调start方法启动Eureka Server
            eurekaServerInitializerConfiguration.start();
            eurekaServerBootstrap.getClass().getDeclaredMethod("initEurekaEnvironment").invoke(eurekaServerBootstrap);
            eurekaServerBootstrap.getClass().getDeclaredMethod("initEurekaServerContext").invoke(eurekaServerBootstrap);
            int registryCount = peerAwareInstanceRegistry.syncUp();
            peerAwareInstanceRegistry.openForTraffic(null, registryCount);
        }

    }

    /**
     * Eureka Client端功能：服务启动、服务注册、服务续约、服务发现、服务下线
     */
    class EurekaClient {

        /**
         * 服务注册
         * 一、Eureka Client发起注册
         * 1.spring-cloud-netflix-eureka-client依赖包下META-INF/spring.factories文件的EnableAutoConfiguration属性会加载{@link EurekaClientAutoConfiguration}类
         *      spring.factories扩展点 >> 加载{@link EurekaClientAutoConfiguration}
         *          初始化{@link EurekaAutoServiceRegistration}自动服务注册类、{@link EurekaServiceRegistry}服务注册接口实现类、{@link CloudEurekaClient}、{@link ApplicationInfoManager}、{@link EurekaRegistration}
         * 2.{@link DiscoveryClient#DiscoveryClient(ApplicationInfoManager, EurekaClientConfig, AbstractDiscoveryClientOptionalArgs, Provider, EndpointRandomizer)}构造函数
         *      对{@link DiscoveryClient#localRegionApps}全局变量调用set(new Applications())方法进行赋值，初始化Applications并缓存到全局变量中
         *      当shouldRegisterWithEureka=false && shouldFetchRegistry=false，即Eureka CLient配置为不注册到Eureka Server且不从Eureka Server获取注册信息时，直接返回
         *      创建scheduler、heartbeatExecutor、cacheRefreshExecutor线程池，scheduler用于处理定时任务、heartbeatExecutor用于执行心跳续约、cacheRefreshExecutor用于刷新服务注册信息
         *      当shouldFetchRegistry=true，即Eureka CLient配置为从Eureka Server获取注册信息时
         *          {@link DiscoveryClient#fetchRegistry(boolean)}全量获取服务注册信息（详见{@link com.tideseng.springcloud.EurekaCommon.EurekaClient#discovery}）
         *      {@link DiscoveryClient#initScheduledTasks()}初始化定时任务
         *          当shouldFetchRegistry=true，即Eureka CLient配置为从Eureka Server获取注册信息时
         *              开启每30秒执行刷新服务注册信息的定时任务{@link DiscoveryClient.CacheRefreshThread}（详见{@link com.tideseng.springcloud.EurekaCommon.EurekaClient#discovery}）
         *          当shouldRegisterWithEureka=true，即Eureka CLient配置为注册到Eureka Server时
         *              开启每30秒执行心跳续约的定时任务{@link DiscoveryClient.HeartbeatThread}（详见{@link com.tideseng.springcloud.EurekaCommon.EurekaClient#renew}）
         *              {@link InstanceInfoReplicator#InstanceInfoReplicator(DiscoveryClient, InstanceInfo, int, int)}创建实例信息复制器
         *              {@link ApplicationInfoManager.StatusChangeListener}创建实例状态变化监听
         *              {@link ApplicationInfoManager#registerStatusChangeListener(ApplicationInfoManager.StatusChangeListener)}注册实例状态变化监听
         *              {@link InstanceInfoReplicator#start(int)}开启实例信息复制器周期性任务，当实例信息变更时重新发起注册（首次进来会延迟40秒发起注册） >> {@link InstanceInfoReplicator#run()}线程run()方法 >> {@link DiscoveryClient#register()}发起注册请求
         * 3.EurekaAutoServiceRegistration基于lifecycle回调start方法发布实例事件
         *      {@link EurekaAutoServiceRegistration#start()}
         *          {@link EurekaServiceRegistry#register(EurekaRegistration)}发起服务注册机制
         *              {@link ApplicationInfoManager#setInstanceStatus(InstanceStatus)}发布实例状态变化事件
         *                  {@link ApplicationInfoManager.StatusChangeListener#notify(StatusChangeEvent)}实例状态变化监听
         *                      {@link InstanceInfoReplicator#onDemandUpdate()}实例状态变化，异步提交任务执行run方法 >> {@link InstanceInfoReplicator#run()}线程run()方法 >> {@link DiscoveryClient#register()}发起注册请求 >> {@link AbstractJerseyEurekaHttpClient#register(InstanceInfo)}发起注册请求
         *          {@link AbstractApplicationContext#publishEvent(ApplicationEvent)}发布实例注册事件
         * 二、Eureka Server处理请求
         *      {@link ApplicationResource#addInstance(InstanceInfo, String)}处理服务注册请求
         *          {@link InstanceRegistry#register(InstanceInfo, boolean)}服务注册 >> {@link PeerAwareInstanceRegistryImpl#register(InstanceInfo, boolean)}服务注册
         *              {@link AbstractInstanceRegistry#register(InstanceInfo, int, boolean)}服务注册逻辑
         *              {@link PeerAwareInstanceRegistryImpl#replicateToPeers(PeerAwareInstanceRegistryImpl.Action, String, String, InstanceInfo, InstanceStatus, boolean)}服务同步逻辑（详见服务同步）
         * 三、Eureka Server存储服务地址
         *      {@link AbstractInstanceRegistry#register(InstanceInfo, int, boolean)}
         *          {@link ReentrantReadWriteLock.ReadLock#lock()}对读锁加锁
         *          {@link AbstractInstanceRegistry#registry}根据实例信息的用户名从registry一级缓存中获取map对象，不存在时创建 （registry变量结构：ConcurrentHashMap<String, Map<String, Lease<InstanceInfo>>>）
         *          {@link gMap.get(registrant.getId())}根据实例Id获取实例信息，不存在时调用{@link AbstractInstanceRegistry#updateRenewsPerMinThreshold()}更新服务续约阈值
         *          {@link Lease#Lease(Object, int)}构建新的Lease(封装了InstanceInfo)
         *          {@link gMap.put(registrant.getId(), lease)}更新map中的实例映射
         *          {@link AbstractInstanceRegistry#invalidateCache(String, String, String)}让二级缓存失效 >> {@link ResponseCacheImpl#invalidate(String, String, String)}让二级缓存失效 >> {@link ResponseCacheImpl#invalidate(Key...)}让二级缓存失效
         *          {@link ReentrantReadWriteLock.ReadLock#unlock()}读锁释放锁
         */
        public void register() throws Exception {
            EurekaClientAutoConfiguration eurekaClientAutoConfiguration = new EurekaClientAutoConfiguration(null);
            EurekaClientConfigBean clientConfig = eurekaClientAutoConfiguration.eurekaClientConfigBean(null);

            DiscoveryClient discoveryClient = new DiscoveryClient(null, null, null, null);
            if (!clientConfig.shouldRegisterWithEureka() && !clientConfig.shouldFetchRegistry()) {
                return;
            }

            if (clientConfig.shouldFetchRegistry()) {

            }

        }

        /**
         * 服务续约
         * 一、Eureka Client发起续约
         * {@link DiscoveryClient#initScheduledTasks()}初始化定时任务
         *      当shouldRegisterWithEureka=false，即Eureka CLient配置为注册到Eureka Server时
         *          开启每30秒执行心跳续约的定时任务{@link DiscoveryClient.HeartbeatThread} >> {@link DiscoveryClient.HeartbeatThread#run()}
         *              {@link DiscoveryClient#renew()}发起续约
         *                  {@link AbstractJerseyEurekaHttpClient#sendHeartBeat(String, String, InstanceInfo, InstanceStatus)}发起续约请求
         *                  当续约请求响应404表示未注册时，走注册流程{@link DiscoveryClient#register()} >> {@link AbstractJerseyEurekaHttpClient#register(InstanceInfo)}发起注册请求
         *              {@link DiscoveryClient#lastSuccessfulHeartbeatTimestamp} 将lastSuccessfulHeartbeatTimestamp最后一次心跳续约时间设置为当前时间
         * 二、Eureka Server处理请求
         *      {@link InstanceResource#renewLease(String, String, String, String)}处理服务续约请求
         *          {@link InstanceRegistry#renew(String, String, boolean)} >> {@link PeerAwareInstanceRegistryImpl#renew(String, String, boolean)}
         *              {@link AbstractInstanceRegistry#renew(String, String, boolean)}服务续约逻辑
         *                  {@link registry.get(appName)}根据应用名获取map信息 >> {@link gMap.get(id)}根据实例Id获取实例信息（1.当实例不存在时返回404让客户端重新发起注册）
         *                  {@link AbstractInstanceRegistry#getOverriddenInstanceStatus(InstanceInfo, Lease, boolean)}获取服务端实例的status（2.当服务端的instance的status为UNKONW时返回404让客户端重新发起注册）
         *                  {@link MeasuredRate#increment()}设置每分钟的续约次数
         *                  {@link Lease#renew()}更新lease续约时间
         *              {@link PeerAwareInstanceRegistryImpl#replicateToPeers(PeerAwareInstanceRegistryImpl.Action, String, String, InstanceInfo, InstanceStatus, boolean)}服务同步逻辑（详见服务同步）
         *          {@link InstanceResource#validateDirtyTimestamp(Long, boolean)}验证客户端lastDirtyTimestamp和本地lastDirtyTimestamp
         *              {@link AbstractInstanceRegistry#getInstanceByAppAndId(String, String, boolean)}根据应用名和实例ID获取本地instance实例信息
         *              当lastDirtyTimestamp > appInfo.getLastDirtyTimestamp()时返回404让客户端重新发起注册（3）
         *              当lastDirtyTimestamp < appInfo.getLastDirtyTimestamp() 且是集群同步请求时，则返回"冲突"状态以本地的时间大的为准
         */
        public void renew() throws Exception {

        }

        /**
         * 服务发现/获取
         * 一、Eureka Client启动发起全量获取
         * {@link DiscoveryClient#DiscoveryClient(ApplicationInfoManager, EurekaClientConfig, AbstractDiscoveryClientOptionalArgs, Provider, EndpointRandomizer)}构造函数
         *      当shouldFetchRegistry=true，即Eureka CLient配置为从Eureka Server获取注册信息时
         *          {@link DiscoveryClient#fetchRegistry(boolean)}全量获取服务注册信息（当设置了禁用增量刷新、强制全量刷新、配置VIP地址、本地缓存为空、缓存服务数量为0、第一次获取等的时候，进行全量获取）
         * 二、Eureka Client定时发起增量获取
         * {@link DiscoveryClient#DiscoveryClient(ApplicationInfoManager, EurekaClientConfig, AbstractDiscoveryClientOptionalArgs, Provider, EndpointRandomizer)}构造函数
         * {@link DiscoveryClient#initScheduledTasks()}初始化定时任务
         *      当shouldFetchRegistry=true，即Eureka CLient配置为从Eureka Server获取注册信息时，开启每30秒执行刷新服务注册信息的定时任务{@link DiscoveryClient.CacheRefreshThread}
         *          {@link DiscoveryClient.CacheRefreshThread#run()} >> {@link DiscoveryClient#refreshRegistry()}获取服务列表
         *              {@link DiscoveryClient#fetchRegistry(boolean)} // 服务获取逻辑
         *                  {@link DiscoveryClient#getAndStoreFullRegistry()}全量刷新
         *                      {@link AbstractJerseyEurekaHttpClient#getApplications(String...)}发起全量获取请求
         *                      {@link DiscoveryClient#filterAndShuffle(Applications)} >> {@link Applications#shuffleInstances(boolean, boolean, Map, EurekaClientConfig, InstanceRegionChecker)}根据shouldFilterOnlyUpInstances配置默认清除非UP状态的实例
         *                      {@link localRegionApps.set()}将可用的服务设置到本地缓存中（使用AtomicReference类型做存储）
         *                  {@link DiscoveryClient#getAndUpdateDelta(Applications)}增量刷新
         *                      {@link AbstractJerseyEurekaHttpClient#getDelta(String...)}发起增量获取请求
         *                      当增量为空时则全量刷新一次
         *                      当增量不为空时将增量合并到本地缓存
         *                          {@link DiscoveryClient#updateDelta(Applications)}将增量合并到本地缓存并清除非UP状态的实例
         *                          {@link DiscoveryClient#getReconcileHashCode(Applications)}合并计算hashCode
         *                          {@link DiscoveryClient#reconcileAndLogDifference(Applications, String)}当合并后的hashcode与返回的hashcode不一致时重新全量获取
         *                  {@link DiscoveryClient#onCacheRefreshed()}发布缓存刷新事件
         *                  {@link DiscoveryClient#updateInstanceRemoteStatus()}更新本地应用状态
         *              {@link DiscoveryClient#lastSuccessfulRegistryFetchTimestamp} 将lastSuccessfulRegistryFetchTimestamp最后一次服务获取时间设置为当前时间
         * 三、Eureka Server处理请求
         * {@link ApplicationsResource#getContainers(String, String, String,String, UriInfo, String)}全量刷新请求
         *      {@link PeerAwareInstanceRegistryImpl#shouldAllowAccess(boolean)}判断是否可以访问
         *      {@link Key#Key(EntityType, String, KeyType, Version, EurekaAccept, String[])}构建获取全量缓存key（ALL_APPS）
         *      {@link ResponseCacheImpl#getGZIP(Key)}从三级缓存中获取全量缓存
         *      {@link AbstractInstanceRegistry#getApplications()}当二、三及缓存不存在时从一级缓存获取全量信息
         *  {@link ApplicationsResource#getContainerDifferential(String, String, String, String, UriInfo, String)}增量刷新请求
         *      {@link Key#Key(EntityType, String, KeyType, Version, EurekaAccept, String[])}构建获取增量缓存key（ALL_APPS_DELTA ）
         *      {@link ResponseCacheImpl#getGZIP(Key)}从三级缓存中获取全量缓存
         *      {@link AbstractInstanceRegistry#getApplicationDeltas()}当二、三及缓存不存在时从一级缓存获取增量信息
         *          {@link AbstractInstanceRegistry#recentlyChangedQueue}从租约变更记录队列获取近期产生过变化（注册、下线、过期等）的应用实例信息（默认保存3分钟）
         * 四、Eureka Client发起远程调用
         *      {@link DynamicServerListLoadBalancer#updateListOfServers()}更新服务列表
         *          {@link DiscoveryEnabledNIWSServerList#getUpdatedListOfServers()} >> {@link DiscoveryEnabledNIWSServerList#obtainServersViaDiscovery()}获取服务列表
         *              {@link DiscoveryClient#getInstancesByVipAddress(String, boolean, String)}
         *                  {@link this.localRegionApps.get()}
         */
        public void discovery() throws Exception {
            // 服务上线最长感知时间是90s（服务延迟）
            // readOnly是30s同步一次、client每30sfetch一次、ribbon每30s更新一次serverList
            // responseCacheUpdateIntervalMs、registryFetchIntervalSeconds、serverListRefreshInterval

            // 服务下线感知
            // 正常下线同上
            // 非正常下线无限趋近于240s
            // server 每60s清理超过90s未续约得服务 60 + 90 + 90
        }

        /**
         * 服务下线
         * 一、Eureka Client发起下线
         * {@link DiscoveryClient#shutdown()}对象销毁时触发下线（@PreDestroy修饰）
         *      {@link ApplicationInfoManager#unregisterStatusChangeListener(String)}注销实例状态变化监听
         *      {@link DiscoveryClient#cancelScheduledTasks()}取消定时任务（心跳续约、缓存刷新等）
         *      {@link ApplicationInfoManager#setInstanceStatus(InstanceStatus)}设置实例状态为DOWN
         *      {@link DiscoveryClient#unregister()}执行下线逻辑
         *          {@link AbstractJerseyEurekaHttpClient#cancel(String, String)}发起下线请求
         * 二、Eureka Server处理请求
         * {@link InstanceResource#cancelLease(String)}处理下线请求
         *      {@link InstanceRegistry#cancel(String, String, boolean)}服务下线
         *          {@link InstanceRegistry#handleCancelation(String, String, boolean)}发布下线事件
         *          {@link PeerAwareInstanceRegistryImpl#cancel(String, String, boolean)}服务下线
         *              {@link AbstractInstanceRegistry#cancel(String, String, boolean)} >> {@link AbstractInstanceRegistry#internalCancel(String, String, boolean)}服务下线
         *                  {@link EurekaMonitors#increment(boolean)}增加下线次数
         *                  {@link registry.get(appName).remove(id)}从一级缓存移除实例信息
         *                  {@link Lease#cancel()}设置下线时间
         *                  {@link InstanceInfo#setActionType(ActionType)}设置操作类型、添加近期更变记录、更新最后操作时间、清除缓存
         *              {@link PeerAwareInstanceRegistryImpl#replicateToPeers(PeerAwareInstanceRegistryImpl.Action, String, String, InstanceInfo, InstanceStatus, boolean)}集群同步
         *              {@link AbstractInstanceRegistry#updateRenewsPerMinThreshold()}更新自我保护阈值
         */
        public void cancel() throws Exception {
            DiscoveryClient discoveryClient = new DiscoveryClient(null, null, null, null);
            discoveryClient.shutdown();
        }

    }

}
