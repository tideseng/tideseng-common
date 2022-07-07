package com.tideseng.springcloud.sample.openfeign.autoconfiguration;

import org.springframework.cloud.commons.util.SpringFactoryImportSelector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @author jiahuan
 * @create 2022/7/7
 */
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class EnableOperationLogImportSelector extends SpringFactoryImportSelector<EnableOperationLog> {

    @Override
    protected boolean isEnabled() {
        return getEnvironment().getProperty("application.log.auto-configuration.enabled", Boolean.class, Boolean.TRUE);
    }

}
