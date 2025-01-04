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
            "/auth/login",
            "/auth/register",
            "/auth/verify-token",
            "/public/**",
            "/error",
            "/swagger-ui/**",
            "/v3/api-docs/**",
//            "/schedules",
//            "/schedule",
            "/schedules/**",
            "/schedule/**"
//            "/schedules",
//            "/schedule"
    );

    private final List<String> PUBLIC_GET_PATHS = Arrays.asList(
            "/movies/**",
            "/stores/**",
            "/api/movies/**",
            "/api/stores/**",
            "/discounts/public/**"
    );

    private final List<String> ADMIN_PATHS = Arrays.asList(
            "/api/admin/**",
            "/admin/**",
            "/api/system/**",
            "/api/movies/management/**",
            "/api/stores/management/**"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String path = request.getRequestURI();
            log.debug("Processing {} request for path: {}", request.getMethod(), path);

            if (isAdminPath(path)) {
                handleAdminRequest(request, response);
            } else if (!isPublicPath(request)) {
                handleProtectedRequest(request);
            } else {
                log.debug("Public access granted for path: {}", path);
            }
        } catch (Exception ex) {
            log.error("JWT Authentication error", ex);
            handleAuthenticationError(response, ex);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handleAdminRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String token = getJwtFromRequest(request);
        if (!StringUtils.hasText(token) || !jwtTokenProvider.validateToken(token)) {
            throw new RuntimeException("無效的管理員認證");
        }

        UserDetails userDetails = processValidToken(request, token);
        if (!userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("需要管理員權限");
        }
    }

    private void handleProtectedRequest(HttpServletRequest request) {
        String token = getJwtFromRequest(request);
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            processValidToken(request, token);
        } else {
            log.debug("No valid JWT token found for protected path");
        }
    }

    private UserDetails processValidToken(HttpServletRequest request, String jwt) {
        try {
            String email = jwtTokenProvider.getEmailFromToken(jwt);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (userDetails != null && userDetails.isEnabled()) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("User authenticated successfully: {}", email);
                return userDetails;
            } else {
                log.warn("User not found or disabled: {}", email);
                throw new RuntimeException("使用者不存在或已被停用");
            }
        } catch (Exception e) {
            log.error("處理 JWT Token 時發生錯誤", e);
            throw new RuntimeException("處理認證時發生錯誤");
        }
    }

    private void handleAuthenticationError(HttpServletResponse response, Exception e) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"認證失敗\",\"message\":\"" + e.getMessage() + "\",\"timestamp\":\"" +
                java.time.LocalDateTime.now() + "\"}");
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
        String contextPath = request.getContextPath();

        if (StringUtils.hasText(contextPath) && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        String finalPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

        boolean isPublic = PUBLIC_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, finalPath));

        if (!isPublic && "GET".equalsIgnoreCase(method)) {
            isPublic = PUBLIC_GET_PATHS.stream()
                    .anyMatch(pattern -> pathMatcher.match(pattern, finalPath));
        }

        log.debug(isPublic ? "Public access granted for path: {}" : "Protected path access attempt: {}", finalPath);
        return isPublic;
    }

    private boolean isAdminPath(String path) {
        return ADMIN_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/public/") ||
                path.equals("/error") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/");
    }
}
