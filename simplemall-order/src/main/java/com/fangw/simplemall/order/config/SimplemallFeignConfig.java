package com.fangw.simplemall.order.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Configuration
public class SimplemallFeignConfig {
    /**
     * feign在远程调用之前会执行所有的RequestInterceptor拦截器
     * 
     * @return
     */
    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                // 1、使用 RequestContextHolder 拿到请求数据，RequestContextHolder底层使用过线程共享数据 ThreadLocal<RequestAttributes>
                ServletRequestAttributes attributes =
                    (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    // 2、同步请求头数据，Cookie
                    String cookie = request.getHeader("Cookie");

                    // 给新请求同步了老请求的cookie
                    requestTemplate.header("Cookie", cookie);
                }
            }
        };
    }
}
