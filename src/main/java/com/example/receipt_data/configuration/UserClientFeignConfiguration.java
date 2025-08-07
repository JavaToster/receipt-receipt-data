package com.example.receipt_data.configuration;

import com.example.receipt_data.configuration.interceptors.JwtTokenInterceptor;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.example.receipt_data.clients")
public class UserClientFeignConfiguration {
    @Bean
    public ErrorDecoder errorDecoder(){
        return new FeignErrorDecoder();
    }

    @Bean
    public RequestInterceptor requestInterceptor(){
        return new JwtTokenInterceptor();
    }
}
