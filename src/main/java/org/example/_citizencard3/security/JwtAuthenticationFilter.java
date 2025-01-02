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
            "/system/**",
            "/api/schedules",
            "/api/schedule",
            "/api/schedules/**",
            "/api/schedule/**",
            "/schedules",
            "/schedule",
            "/schedules/**",
            "/schedule/**"
    );

    private final List<String> PUBLIC_GET_PATHS = Arrays.asList(
            "/movies/**",
            "/stores/**",
            "/api/movies/**",
            "/api/stores/**",
            "/schedules",
            "/schedule",
            "/schedules/**",
            "/schedule/**",
            "/api/schedules",
            "/api/schedule",
            "/api/schedules/**",
            "/api/schedule/**",
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
            String method = request.getMethod();
            log.debug("Processing {} request for path: {}", method, path);

            // Handle OPTIONS requests and public paths
            if ("OPTIONS".equalsIgnoreCase(method) || isPublicPath(request)) {
                log.debug("Allowing public access for: {}", path);
                filterChain.doFilter(request, response);
                return;
            }

            String jwt = getJwtFromRequest(request);
            if (StringUtils.hasText(jwt)) {
                if (jwtTokenProvider.validateToken(jwt)) {
                    processValidToken(request, jwt);
                } else {
                    log.warn("Invalid JWT token");
                    handleAuthenticationError(response, new RuntimeException("Invalid JWT token"));
                    return;
                }
            } else if (!isPublicPath(request)) {
                log.debug("Protected path requires authentication: {}", path);
                handleAuthenticationError(response, new RuntimeException("Authentication required"));
                return;
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage());
            handleAuthenticationError(response, e);
        }
    }

    private void processValidToken(HttpServletRequest request, String jwt) {
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
            throw new RuntimeException("User not found or disabled");
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

        log.debug(isPublic ? "Public access granted for path: {}" : "Protected path access attempt: {}", path);

        return isPublic;
    }
}