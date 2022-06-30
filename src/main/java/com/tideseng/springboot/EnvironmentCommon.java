package com.tideseng.springboot;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.StandardServletEnvironment;

/**
 * @author jiahuan
 * @create 2022/6/30
 */
@Component
public class EnvironmentCommon implements EnvironmentAware {

    /**
     * Environment中的PropertySource属性源列表，当从Environment获取属性时，遍历PropertySource属性源列表，逐个进行获取，获取不到时遍历下一个
     * 0 = "configurationProperties"
     * 1 = "servletConfigInitParams"
     * 2 = "servletContextInitParams"
     * 3 = "systemProperties"
     * 4 = "systemEnvironment"
     * 5 = "random"
     * 6 = "springCloudClientHostInfo"
     * 7 = "applicationConfig: [classpath:/application-local.yml]"
     * 8 = "springCloudDefaultProperties"
     * 9 = "applicationConfig: [classpath:/bootstrap.yml]"
     * 10 = "bootstrapProperties"
     * 11 = "defaultProperties"
     */
    @Override
    public void setEnvironment(Environment environment) {
        StandardServletEnvironment servletEnvironment = (StandardServletEnvironment) environment;
        MutablePropertySources propertySources = servletEnvironment.getPropertySources();
        propertySources.stream().forEach(e -> System.out.println(e.getName()));
    }

}
