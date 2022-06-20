package com.tideseng.springcloud.sample.openfeign.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Feign的请求拦截器，拦截请求，处理Post请求的Body体、param参数、head头信息
 * @author jiahuan
 * @create 2022/6/20
 */
public class FeignClientRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // 如果不是Request请求则不进行处理
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        // 设置header头信息，如：添加令牌信息
        template.header("Authorization", "Bearer xxx");

        // 设置header头信息（拷贝原Request的请求头信息）
        HttpServletRequest request = attributes.getRequest();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                String values = request.getHeader(name);
                template.header(name, values);
            }
        }

        // 设置Body体
        String method = template.method();
        if (template.requestBody().asBytes() != null) {
            int length = template.requestBody().asBytes().length;
            if (length > 0) {
                String body = template.requestBody().asString();
                if (!StringUtils.isEmpty(body)) {
                    try {
                        Object object = new Object();
                        // 更换Body体，需要先序列化对象，再转为字节数组【这里仅仅是为了演示】
                        template.body(Request.Body.encoded(new ObjectMapper().writeValueAsString(object).getBytes(template.requestCharset()), template.requestCharset()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // 设置param参数
        Map<String, Collection<String>> queries = template.queries();
        if (queries != null) {
            Map<String, Collection<String>> newQueries = new HashMap<>();
            for (Map.Entry<String, Collection<String>> entry : queries.entrySet()) {
                if("userIds".equals(entry.getKey())) {
                    Collection<String> value = new ArrayList<>();
                    Collection<String> originValue = entry.getValue();
                    if(!StringUtils.isEmpty(originValue)) {
                        List<String> list = (List) originValue;
                        Arrays.asList(StringUtils.commaDelimitedListToStringArray(list.get(0)));
                        newQueries.put("userIdList", Arrays.asList(StringUtils.commaDelimitedListToStringArray(list.get(0))));
                    }
                }
            }
            // 替换原有的param参数前，需要将原对象设为空，否则会追加
            template.queries(null);
            // 重新设置param请求参数
            template.queries(newQueries);
        }
    }

}
