package com.onlinetrain.config;

import com.onlinetrain.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    /**
     * 配置静态资源映射（上传文件）
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }

    /**
     * 配置登录拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns(
                        "/api/**"
                )
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register",
                        "/api/user/wx-login",
                        "/api/admin/auth/login",
                        "/api/courses",
                        "/api/courses/*/chapters",
                        "/api/courses/*/chapters/*",
                        "/api/categories",
                        "/api/categories/**",
                        "/api/exams",
                        "/api/exams/**",
                        "/api/banners",
                        "/api/banners/**",
                        "/api/news",
                        "/api/news/**",
                        "/api/payment/callback/**",
                        "/api/wx/oauth-callback",
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v2/api-docs/**",
                        "/v3/api-docs/**",
                        "/favicon.ico",
                        "/error"
                );
    }
}
