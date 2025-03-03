package com.dsapkl.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${file.dir}")
    private String fileDir;

    private final PageViewLoggingInterceptor pageViewLoggingInterceptor;

    public WebConfig(PageViewLoggingInterceptor pageViewLoggingInterceptor) {
        this.pageViewLoggingInterceptor = pageViewLoggingInterceptor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:///C:/pkl/images/")
                .setCachePeriod(3600)
                .resourceChain(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(pageViewLoggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/css/**", 
                    "/js/**", 
                    "/images/**", 
                    "/error",
                    "/ws-analytics/**",
                    "/api/**"
                );
    }
}
