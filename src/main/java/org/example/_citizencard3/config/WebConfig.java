package org.example._citizencard3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods}")
    private String[] allowedMethods;

    @Value("${app.cors.allowed-headers}")
    private String[] allowedHeaders;

    @Value("${app.cors.exposed-headers}")
    private String[] exposedHeaders;

    @Value("${app.cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age}")
    private long maxAge;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Schedule相關端點的CORS配置
        String[] schedulePatterns = {
                "/api/schedules/**",
                "/api/schedule/**",
                "/api/schedules",
                "/api/schedule",
                "/schedules/**",
                "/schedule/**"
        };

        for (String pattern : schedulePatterns) {
            registry.addMapping(pattern)
                    .allowedOrigins("*")
                    .allowedMethods("GET", "HEAD", "OPTIONS")
                    .allowedHeaders("*")
                    .exposedHeaders("*")
                    .allowCredentials(false)
                    .maxAge(maxAge);
        }

        // 一般API的CORS配置
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods(allowedMethods)
                .allowedHeaders(allowedHeaders)
                .exposedHeaders(exposedHeaders)
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 靜態資源處理
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
                .resourceChain(true)
                .addResolver(new VersionResourceResolver()
                        .addContentVersionStrategy("/**"))
                .addResolver(new EncodedResourceResolver())
                .addResolver(new PathResourceResolver());

        // 公共資源
        registry.addResourceHandler("/public/**")
                .addResourceLocations("classpath:/public/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                .resourceChain(true)
                .addResolver(new PathResourceResolver());

        // 上傳文件資源
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .resourceChain(true)
                .addResolver(new PathResourceResolver());

        // 用戶頭像資源
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:avatars/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
                .resourceChain(true)
                .addResolver(new PathResourceResolver());

        // API文檔資源
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .resourceChain(true)
                .addResolver(new PathResourceResolver());

        // 系統資源
        registry.addResourceHandler("/system/**")
                .addResourceLocations("classpath:/system/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .resourceChain(true)
                .addResolver(new VersionResourceResolver()
                        .addContentVersionStrategy("/**"))
                .addResolver(new PathResourceResolver());
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 首頁路由
        registry.addViewController("/")
                .setViewName("forward:/index.html");

        // SPA 路由處理
        registry.addViewController("/admin/**")
                .setViewName("forward:/index.html");
        registry.addViewController("/user/**")
                .setViewName("forward:/index.html");
        registry.addViewController("/auth/**")
                .setViewName("forward:/index.html");
    }
}
