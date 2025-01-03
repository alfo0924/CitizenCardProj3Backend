package org.example._citizencard3.config;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.security.JwtAuthenticationFilter;
import org.example._citizencard3.security.JwtTokenProvider;
import org.example._citizencard3.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    // 公開端點定義
    private static final String[] PUBLIC_URLS = {
            "/auth/login",
            "/auth/register",
            "/auth/verify-token",
            "/auth/password-reset",
            "/auth/password-reset-confirm"
    };

    // 管理員端點定義
    private static final String[] ADMIN_URLS = {
            "/api/system/**",
            "/admin/**",
            "/api/system/dashboard",
            "/api/system/status",
            "/api/system/distributions",
            "/api/system/cache/**"
    };

    // Schedule 相關端點定義
    private static final String[] SCHEDULE_PUBLIC_URLS = {
            "/api/schedules/**",
            "/api/schedule/**",
            "/schedules/**",
            "/schedule/**"
    };

    // 需要認證的端點定義
    private static final String[] AUTHENTICATED_URLS = {
            "/users/**",
            "/wallets/**",
            "/movie-tickets/**",
            "/movie-ticket-qrcodes/**",
            "/discount-coupons/**",
            "/discount-coupon-qrcodes/**"
    };

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${app.cors.exposed-headers}")
    private String exposedHeaders;

    @Value("${app.cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age}")
    private long maxAge;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 配置 CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 配置 Session 管理
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置請求授權
                .authorizeHttpRequests(auth -> auth
                        // 公開端點
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Schedule 相關端點
                        .requestMatchers(SCHEDULE_PUBLIC_URLS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/schedules/movie/**",
                                "/api/schedules/available",
                                "/api/schedules/date-range",
                                "/api/schedules/hall/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/schedule/movie/**",
                                "/api/schedule/available",
                                "/api/schedule/date-range",
                                "/api/schedule/hall/**").permitAll()

                        // 公開的電影和商店信息
                        .requestMatchers(HttpMethod.GET, "/movies/**", "/stores/**").permitAll()

                        // 需要認證的端點
                        .requestMatchers(AUTHENTICATED_URLS).authenticated()

                        // 管理員端點
                        .requestMatchers(ADMIN_URLS).hasRole("ADMIN")

                        // 默認策略
                        .anyRequest().authenticated()
                )
                // 添加 JWT 過濾器
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                // 配置異常處理
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\":\"未授權\",\"message\":\"請先登入\",\"timestamp\":\"" +
                                    java.time.LocalDateTime.now() + "\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\":\"存取被拒絕\",\"message\":\"權限不足\",\"timestamp\":\"" +
                                    java.time.LocalDateTime.now() + "\"}");
                        })
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        configuration.setExposedHeaders(Arrays.asList(exposedHeaders.split(",")));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    }
}