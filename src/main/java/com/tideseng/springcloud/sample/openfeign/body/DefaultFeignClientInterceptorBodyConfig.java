package com.tideseng.springcloud.sample.openfeign.body;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 默认的FeignClient拦截Body体配置，在请求前自定义修改Body体
 * @author jiahuan
 * @create 2022/6/14
 */
public abstract class DefaultFeignClientInterceptorBodyConfig implements RequestInterceptor {

    /**
     * OpenFeign拦截器的拦截方法
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            // 获取要更换的Body对象
            Object object = doFilter(requestTemplate, request);
            if(object != null) {
                try {
                    // 更换Body体，需要先序列化对象，再转为字节数组【这里仅仅是为了演示】
                    requestTemplate.body(Request.Body.encoded(new ObjectMapper().writeValueAsString(object).getBytes(requestTemplate.requestCharset()), requestTemplate.requestCharset()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 抽象方法，由子类实现；返回值不为空时则替换body体、返回值为空时不进行任何处理
     * @param requestTemplate
     * @param request
     * @return
     */
    public abstract Object doFilter(RequestTemplate requestTemplate, HttpServletRequest request);

}