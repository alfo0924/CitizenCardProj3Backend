package org.example._citizencard3.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3009")
                .allowedMethods(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
                .allowedHeaders(
                        "Authorization",
                        "Content-Type",
                        "X-Requested-With",
                        "Accept",
                        "Origin"
                )
                .exposedHeaders(
                        "Authorization"
                )
                .allowCredentials(false)
                .maxAge(3600L);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 靜態資源處理
        registry.addResourceHandler("/**")
                .addResourceLocations(
                        "classpath:/static/",
                        "classpath:/public/",
                        "classpath:/resources/"
                )
                .setCachePeriod(3600);

        // API文檔資源
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                .setCachePeriod(3600);

        // 系統資源
        registry.addResourceHandler("/system/**")
                .addResourceLocations("classpath:/system/")
                .setCachePeriod(3600)
                .resourceChain(true);
    }
}
