package com.tideseng.springcloud.sample.openfeign.autoconfiguration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author jiahuan
 * @create 2022/7/7
 */
@EnableOperationLog
@SpringBootApplication
public class AutoConfigurationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoConfigurationApplication.class, args);
    }

}
