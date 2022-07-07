package com.tideseng.springcloud.sample.openfeign.autoconfiguration;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author jiahuan
 * @create 2022/7/7
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(EnableOperationLogImportSelector.class)
public @interface EnableOperationLog {
}
