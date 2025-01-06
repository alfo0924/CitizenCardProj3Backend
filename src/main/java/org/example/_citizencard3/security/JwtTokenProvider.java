package org.example._citizencard3.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.example._citizencard3.config.JwtConfig;
import org.example._citizencard3.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;
    private final UserDetailsServiceImpl userDetailsService;
    private final Key key;

    public JwtTokenProvider(JwtConfig jwtConfig, UserDetailsServiceImpl userDetailsService) {
        this.jwtConfig = jwtConfig;
        this.userDetailsService = userDetailsService;
        this.key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails);
    }

    public String generateToken(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return generateToken(userDetails);
    }

    public String generateToken(UserDetails userDetails) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());

            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", userDetails.getUsername());
            claims.put("created", now);

            // 獲取用戶的所有權限
            String authorities = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            // 獲取主要角色（第一個角色）
            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("ROLE_USER");

            claims.put("role", role);
            claims.put("authorities", authorities);

            log.debug("Generating token for user: {} with role: {}", userDetails.getUsername(), role);

            return Jwts.builder()
                    .setClaims(claims)
                    .setIssuer(jwtConfig.getIssuer())
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(key, SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            log.error("Error generating token for user: {}", userDetails.getUsername(), e);
            throw new CustomException("生成令牌時發生錯誤", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String getEmailFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String email = claims.getSubject();
            log.debug("Extracted email from token: {}", email);
            return email;
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            throw new CustomException("令牌已過期", HttpStatus.UNAUTHORIZED);
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new CustomException("無效的令牌", HttpStatus.UNAUTHORIZED);
        }
    }

    public boolean validateToken(String token) {
        try {
            if (token == null) {
                return false;
            }

            Claims claims = getClaimsFromToken(token);

            // 檢查是否過期
            if (claims.getExpiration().before(new Date())) {
                log.warn("JWT token is expired");
                return false;
            }

            // 驗證簽發者
            if (jwtConfig.isValidateIssuer() &&
                    !claims.getIssuer().equals(jwtConfig.getIssuer())) {
                log.warn("JWT issuer is invalid");
                return false;
            }

            log.debug("JWT token is valid");
            return true;

        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            throw new CustomException("令牌已過期", HttpStatus.UNAUTHORIZED);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new CustomException("無效的令牌", HttpStatus.UNAUTHORIZED);
        }
    }

    public Authentication getAuthentication(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String email = claims.getSubject();
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            log.debug("Creating authentication for user: {} with authorities: {}",
                    email, userDetails.getAuthorities());

            return new UsernamePasswordAuthenticationToken(
                    userDetails, "", userDetails.getAuthorities());
        } catch (Exception e) {
            log.error("Error creating authentication from token", e);
            throw new CustomException("認證失敗", HttpStatus.UNAUTHORIZED);
        }
    }

    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new CustomException("令牌已過期", HttpStatus.UNAUTHORIZED);
        } catch (JwtException e) {
            throw new CustomException("無效的令牌", HttpStatus.UNAUTHORIZED);
        }
    }

    public long getExpirationTime() {
        return jwtConfig.getExpiration() / 1000;
    }

    public void invalidateToken(String token) {
        if (token != null && validateToken(token)) {
            // 這裡可以實現令牌黑名單邏輯
            // 例如將令牌加入 Redis 黑名單
            log.debug("Token invalidated: {}", token);
        }
    }

    public String refreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String email = claims.getSubject();

            // 檢查是否需要刷新
            Date tokenExpiration = claims.getExpiration();
            Date refreshThreshold = new Date(System.currentTimeMillis() + jwtConfig.getMinimumRefreshInterval());

            if (tokenExpiration.before(refreshThreshold)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                String newToken = generateToken(userDetails);
                log.debug("Token refreshed for user: {}", email);
                return newToken;
            }

            return token;
        } catch (Exception e) {
            log.error("Error refreshing token", e);
            throw new CustomException("刷新令牌失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}