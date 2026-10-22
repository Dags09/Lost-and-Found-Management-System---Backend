package com.lostandfound.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserRateLimitInterceptor userRateLimit;

    public WebMvcConfig(UserRateLimitInterceptor userRateLimit) {
        this.userRateLimit = userRateLimit;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userRateLimit).addPathPatterns("/v1/**");
    }
}