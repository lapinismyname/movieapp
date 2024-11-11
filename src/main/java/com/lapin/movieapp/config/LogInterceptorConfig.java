package com.lapin.movieapp.config;

import com.lapin.movieapp.interceptor.LogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LogInterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //register an interceptor with the registry, name of the Interceptor: RequestInterceptor:
        registry.addInterceptor(new LogInterceptor());
        //matter fact we can register any number of interceptors...
    }
}
