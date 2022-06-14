package com.tideseng.springcloud.sample.openfeign.body;

import com.tideseng.Application;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * FeignClient，注解中指定该FeignClient的配置类，添加Body体拦截器
 * @see InstanceFeignClientInterceptorBodyConfig
 * @author jiahuan
 * @create 2022/6/14
 */
@FeignClient(
        value = "data-model",
        contextId = "instanceClient",
        path = "/api/data-model/instance",
        configuration = {InstanceFeignClientInterceptorBodyConfig.class}
)
public interface InstanceClient {

    @PostMapping("/list")
    List<Application> getList(@RequestBody Application aplication);

}
