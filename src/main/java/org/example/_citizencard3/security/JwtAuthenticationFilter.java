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
            "/system/**"
    );

    private final List<String> PUBLIC_GET_PATHS = Arrays.asList(
            "/movies/**",
            "/stores/**",
            "/schedules/**",
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

            String jwt = getJwtFromRequest(request);
            handleAuthentication(request, jwt);
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            handleAuthenticationError(response, e);
        }
    }

    private void handleAuthentication(HttpServletRequest request, String jwt) {
        if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
            try {
                String email = jwtTokenProvider.getEmailFromToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (userDetails != null && userDetails.isEnabled()) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("User authenticated successfully: {}", email);
                }
            } catch (Exception e) {
                log.error("無法設置用戶認證: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
    }

    private void handleAuthenticationError(HttpServletResponse response, Exception e) {
        log.error("JWT認證處理失敗: {}", e.getMessage());
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write("{\"error\":\"認證失敗\",\"message\":\"" + e.getMessage() + "\"}");
        } catch (IOException ex) {
            log.error("寫入錯誤響應失敗", ex);
        }
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

        // 移除context path以進行比對
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        // 檢查基本公開路徑
        String finalPath1 = path;
        boolean isPublic = PUBLIC_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, finalPath1));

        // 檢查GET請求的特殊公開路徑
        if (!isPublic && "GET".equalsIgnoreCase(method)) {
            String finalPath = path;
            isPublic = PUBLIC_GET_PATHS.stream()
                    .anyMatch(pattern -> pathMatcher.match(pattern, finalPath));
        }

        if (isPublic) {
            log.debug("公開訪問路徑: {}", path);
        }

        return isPublic;
    }
}
