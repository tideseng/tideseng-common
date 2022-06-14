package com.tideseng.springcloud.sample.openfeign.body;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tideseng.Application;
import feign.RequestTemplate;

import javax.servlet.http.HttpServletRequest;

/**
 * {@link InstanceClient} FeignClient的拦截器配置类，拦截Body体，只需实现模板方法
 * @see DefaultFeignClientInterceptorBodyConfig
 * @author jiahuan
 * @create 2022/6/14
 */
public class InstanceFeignClientInterceptorBodyConfig extends DefaultFeignClientInterceptorBodyConfig {

    /**
     * 实现模板方法，返回null则表示不需要替换Body、反之则替换
     * @param requestTemplate
     * @param request
     * @return
     */
    @Override
    public Object doFilter(RequestTemplate requestTemplate, HttpServletRequest request) {
        // 拦截指定的远程调用接口
        if(requestTemplate.url().contains("/list")) {
            // 获取原Body数据
            String content = requestTemplate.requestBody().asString();

            try {
                // 反序列化成指定对象，并根据对象属性进行扩展对象【这里仅仅是为了演示】
                ObjectMapper objectMapper = new ObjectMapper();
                Application instanceListVO = objectMapper.readValue(content, Application.class);
                if (instanceListVO != null) {
                    // 根据对象属性进行扩展对象
                    return instanceListVO;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}