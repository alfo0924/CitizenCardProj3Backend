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
            "/api/auth/**",
            "/api/public/**",
            "/error",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    );

    private final List<String> PUBLIC_GET_PATHS = Arrays.asList(
            "/api/movies/**",
            "/api/stores/**",
            "/api/schedules/**",
            "/api/discounts/public/**"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // 檢查是否為公開路徑
            if (isPublicPath(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            String jwt = getJwtFromRequest(request);

            // 處理認證邏輯
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

        // 檢查基本公開路徑
        boolean isPublic = PUBLIC_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        // 檢查GET請求的特殊公開路徑
        if (!isPublic && "GET".equalsIgnoreCase(method)) {
            isPublic = PUBLIC_GET_PATHS.stream()
                    .anyMatch(pattern -> pathMatcher.match(pattern, path));
        }

        if (isPublic) {
            log.debug("公開訪問路徑: {}", path);
        }

        return isPublic;
    }
}
