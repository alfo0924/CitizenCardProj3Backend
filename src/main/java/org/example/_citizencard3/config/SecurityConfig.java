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
            "/auth/password-reset-confirm",
            "/api/public/**",
            "/error"
    };

    // 管理員端點定義 - 更新包含所有管理員相關路徑
    private static final String[] ADMIN_URLS = {
            "/api/system/**",
            "/admin/**",
            "/api/admin/**",
            "/api/movies/management/**",
            "/api/stores/management/**",
            "/api/users/management/**",
            "/api/system/dashboard",
            "/api/system/status",
            "/api/system/distributions",
            "/api/system/cache/**"
    };

    // Schedule 相關端點定義
    private static final String[] SCHEDULE_PUBLIC_URLS = {
            "/schedules/**",
            "/schedule/**",
            "/seats/**"
    };

    // 需要認證的端點定義
    private static final String[] AUTHENTICATED_URLS = {
            "/api/users/**",
            "/api/wallets/**",
            "/api/movie-tickets/**",
            "/api/movie-ticket-qrcodes/**",
            "/api/discount-coupons/**",
            "/api/discount-coupon-qrcodes/**",
            "/users/**",
            "/wallets/**",
            "/movie-tickets/**",
            "/movietickets/**",
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
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 公開端點
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Schedule 相關端點
                        .requestMatchers(SCHEDULE_PUBLIC_URLS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/schedules/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/schedule/**").permitAll()


                        // 公開的電影和商店信息
                        .requestMatchers(HttpMethod.GET, "/api/movies/**", "/api/stores/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/movies/**", "/stores/**").permitAll()

                        // 需要認證的端點
                        .requestMatchers(AUTHENTICATED_URLS).authenticated()

                        // 管理員端點 - 明確指定需要 ADMIN 角色
                        .requestMatchers(ADMIN_URLS).hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 默認策略
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
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
