package com.tideseng.springweb.sample;

import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;

/**
 * @author jiahuan
 * @create 2022/7/1
 */
public class RequestSupport {

    /**
     * 设置参数到Request中
     * 在Tomcat中，HttpServletRequest的实现类是 {@link org.apache.catalina.connector.RequestFacade}，该类有一个request属性，对应着{@link org.apache.catalina.connector.Request}类
     * {@link org.apache.catalina.connector.Request}类有一个coyoteRequest属性，对应着{@link org.apache.coyote.Request}类
     * {@link org.apache.coyote.Request}类有一个headers属性，对应着{@link MimeHeaders}类，可以调用{@link MimeHeaders#addValue(String)}添加请求头的key、调用{@link MessageBytes#setString(String)}添加请求头的value
     * @param request
     * @param key
     * @param value
     */
    public static void setParam2Header(HttpServletRequest request, String key, String value){
        Class<?extends HttpServletRequest> requestClass = request.getClass();
        try {
            // 通过反射获取到org.apache.catalina.connector.RequestFacade中的org.apache.catalina.connector.Request对象
            Field requestField = requestClass.getDeclaredField("request");
            requestField.setAccessible(true);
            Object requestObj = requestField.get(request);

            // 通过反射获取到org.apache.catalina.connector.Request中的org.apache.coyote.Request对象
            Field coyoteRequestField = requestObj.getClass().getDeclaredField("coyoteRequest");
            coyoteRequestField.setAccessible(true);
            Object coyoteRequestObj = coyoteRequestField.get(requestObj);

            // 通过反射获取到org.apache.coyote.Request中的org.apache.tomcat.util.http.MimeHeaders对象
            Field headersField = coyoteRequestObj.getClass().getDeclaredField("headers");
            headersField.setAccessible(true);
            MimeHeaders mimeHeaders = (MimeHeaders) headersField.get(coyoteRequestObj);

            // 根据org.apache.tomcat.util.http.MimeHeaders对象设置请求头
            mimeHeaders.addValue(key).setString(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
