package org.example._citizencard3.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final List<String> PUBLIC_PATHS = Arrays.asList(
            "/auth/**",
            "/public/**",
            "/error",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/system/**",
            "/api/schedules/**",      // Schedule API路徑
            "/api/schedule/**",       // 額外支援單數形式
            "/schedules/**",          // 支援無api前綴
            "/schedule/**"            // 支援無api前綴單數形式
    );

    private final List<String> PUBLIC_GET_PATHS = Arrays.asList(
            "/movies/**",
            "/stores/**",
            "/schedules/**",
            "/schedule/**",           // 支援單數形式
            "/api/schedules/**",      // 完整API路徑
            "/api/schedule/**",       // 支援單數形式
            "/api/schedules",         // 根路徑
            "/api/schedule",          // 根路徑單數形式
            "/discounts/public/**"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String path = request.getRequestURI();
            log.debug("Processing request for path: {}", path);

            // 檢查是否為公開路徑
            if (isPublicPath(request)) {
                log.debug("Public path accessed: {}", path);
                filterChain.doFilter(request, response);
                return;
            }

            // 處理JWT認證
            String jwt = getJwtFromRequest(request);
            if (StringUtils.hasText(jwt)) {
                if (jwtTokenProvider.validateToken(jwt)) {
                    String email = jwtTokenProvider.getEmailFromToken(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    if (userDetails != null && userDetails.isEnabled()) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("User authenticated successfully: {}", email);
                    } else {
                        log.warn("User not found or disabled: {}", email);
                        handleAuthenticationError(response, new RuntimeException("User not found or disabled"));
                        return;
                    }
                } else {
                    log.warn("Invalid JWT token");
                    handleAuthenticationError(response, new RuntimeException("Invalid JWT token"));
                    return;
                }
            } else {
                log.debug("No JWT token found in request");
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage());
            handleAuthenticationError(response, e);
        }
    }

    private void handleAuthenticationError(HttpServletResponse response, Exception e) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"認證失敗\",\"message\":\"" + e.getMessage() + "\"}");
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean isPublicPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 允許所有OPTIONS請求
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // 處理上下文路徑
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        String finalPath = path;

        // 檢查是否為公開路徑
        boolean isPublic = PUBLIC_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, finalPath));

        // 如果不是公開路徑，檢查是否為GET請求的公開路徑
        if (!isPublic && "GET".equalsIgnoreCase(method)) {
            isPublic = PUBLIC_GET_PATHS.stream()
                    .anyMatch(pattern -> pathMatcher.match(pattern, finalPath));
        }

        if (isPublic) {
            log.debug("Public access path: {}", path);
        }

        return isPublic;
    }
}
