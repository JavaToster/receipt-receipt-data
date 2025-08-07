package com.example.receipt_data.configuration.interceptors;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class JwtTokenInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate){
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes){
            HttpServletRequest request = servletRequestAttributes.getRequest();

            String authHeader = request.getHeader("Authorization");
            if (isValid(authHeader)){
                requestTemplate.header("Authorization", authHeader);
            }
        }
    }

    private boolean isValid(String authHeader){
        return authHeader != null && authHeader.startsWith("Bearer ");
    }
}
