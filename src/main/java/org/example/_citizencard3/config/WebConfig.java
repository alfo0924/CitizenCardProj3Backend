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
    // API endpoints CORS configuration
    registry.addMapping("/api/**")
        .allowedOrigins(allowedOrigins.split(","))
        .allowedMethods(allowedMethods)
        .allowedHeaders(allowedHeaders)
        .exposedHeaders(exposedHeaders)
        .allowCredentials(allowCredentials)
        .maxAge(maxAge);

    // Auth endpoints specific CORS configuration
    registry.addMapping("/auth/**")
        .allowedOrigins(allowedOrigins.split(","))
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("Authorization", "Content-Type", "X-Requested-With")
        .exposedHeaders("Authorization")
        .allowCredentials(true)
        .maxAge(3600);

    // Public resources CORS configuration
    registry.addMapping("/public/**")
//        .allowedOrigins("*")
        .allowedOriginPatterns("*")  // 使用這個替代上面的Origins("*")
        .allowedMethods("GET", "HEAD", "OPTIONS")
        .allowedHeaders("*")
        .maxAge(maxAge);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // API resources
//        registry.addResourceHandler("/api/**")
//                .addResourceLocations("classpath:/api/")
//                .setCacheControl(CacheControl.noCache())
//                .resourceChain(true)
//                .addResolver(new PathResourceResolver());
    registry.addResourceHandler("/api/images/**", "/images/**")
            .addResourceLocations("file:C:/Users/user/IdeaProjects/CitiZenCard3.1/src/main/resources/static/images/")
            .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS));
    registry.addResourceHandler("/api/images/**")
            .addResourceLocations("classpath:/static/images/")
            .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS));
    registry.addResourceHandler("/api/images/**")
            .addResourceLocations("file:C:/Users/user/IdeaProjects/CitiZenCard3.1/src/main/resources/static/images/")
            .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS));

    registry.addResourceHandler("/images/**")
            .addResourceLocations("classpath:/static/images/")
            .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS));

    // Auth resources
    registry.addResourceHandler("/auth/**")
        .addResourceLocations("classpath:/auth/")
        .setCacheControl(CacheControl.noCache())
        .resourceChain(true)
        .addResolver(new PathResourceResolver());

    // Static resources
    registry.addResourceHandler("/static/**", "/assets/**")
        .addResourceLocations("classpath:/static/", "classpath:/assets/")
        .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
        .resourceChain(true)
        .addResolver(new PathResourceResolver());

    // Public resources
    registry.addResourceHandler("/public/**")
        .addResourceLocations("classpath:/public/")
        .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
        .resourceChain(true)
        .addResolver(new PathResourceResolver());

    // Uploaded files
    registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:uploads/", "file:uploads/images/")
            .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
            .resourceChain(true)
            .addResolver(new PathResourceResolver());

    // Swagger documentation
    registry.addResourceHandler("/swagger-ui/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
        .resourceChain(true)
        .addResolver(new PathResourceResolver());

    // Error pages
    registry.addResourceHandler("/error/**")
        .addResourceLocations("classpath:/error/")
        .setCacheControl(CacheControl.noCache())
        .resourceChain(true)
        .addResolver(new PathResourceResolver());
    // 新增臨時文件存儲
    registry.addResourceHandler("/temp/**")
            .addResourceLocations("file:temp/")
            .setCacheControl(CacheControl.noStore())
            .resourceChain(true)
            .addResolver(new PathResourceResolver());
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    // SPA routes
    String[] spaRoutes = {
        "/",
        "/login",
        "/register",
        "/profile",
        "/verify-token",
        "/movies",
        "/schedules",
        "/tickets",
        "/wallet",
        "/admin"
//                "/movies/**",
//                "/schedules/**",
//                "/tickets/**",
//                "/wallet/**",
//                "/admin/**"
    };

    for (String route : spaRoutes) {
      registry.addViewController(route)
          .setViewName("forward:/index.html");
    }
  }
}