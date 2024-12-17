package org.example._citizencard3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

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
        // API端點的CORS配置
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods(allowedMethods)
                .allowedHeaders(allowedHeaders)
                .exposedHeaders(exposedHeaders)
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);

        // 公開資源的CORS配置
        registry.addMapping("/public/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "HEAD", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(maxAge);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // API資源
        registry.addResourceHandler("/api/**")
                .addResourceLocations("classpath:/api/")
                .setCacheControl(CacheControl.noCache())
                .resourceChain(true)
                .addResolver(new PathResourceResolver());

        // 靜態資源
        registry.addResourceHandler("/static/**", "/assets/**")
                .addResourceLocations("classpath:/static/", "classpath:/assets/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
                .resourceChain(true)
                .addResolver(new PathResourceResolver());

        // 公共資源
        registry.addResourceHandler("/public/**")
                .addResourceLocations("classpath:/public/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                .resourceChain(true)
                .addResolver(new PathResourceResolver());

        // 上傳文件
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .resourceChain(true)
                .addResolver(new PathResourceResolver());

        // Swagger文檔
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver());

        // 錯誤頁面
        registry.addResourceHandler("/error/**")
                .addResourceLocations("classpath:/error/")
                .setCacheControl(CacheControl.noCache())
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // SPA路由
        String[] spaRoutes = {
                "/",
                "/login",
                "/register",
                "/profile",
                "/movies/**",
                "/schedules/**",
                "/tickets/**",
                "/wallet/**",
                "/admin/**"
        };

        for (String route : spaRoutes) {
            registry.addViewController(route)
                    .setViewName("forward:/index.html");
        }
    }
}
