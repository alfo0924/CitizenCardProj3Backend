package org.example._citizencard3.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.example._citizencard3.config.JwtConfig;
import org.example._citizencard3.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        return generateToken(userDetails.getUsername());
    }

    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", email);
        claims.put("created", now);
        claims.put("role", "ROLE_USER");

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            throw new CustomException("令牌已過期", HttpStatus.UNAUTHORIZED);
        } catch (JwtException e) {
            throw new CustomException("無效的令牌", HttpStatus.UNAUTHORIZED);
        }
    }

    public boolean validateToken(String token) {
        try {
            if (token == null) {
                return false;
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 檢查令牌是否過期
            return !claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            throw new CustomException("令牌已過期", HttpStatus.UNAUTHORIZED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException("無效的令牌", HttpStatus.UNAUTHORIZED);
        }
    }

    public Authentication getAuthentication(String token) {
        try {
            String email = getEmailFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        } catch (Exception e) {
            throw new CustomException("認證失敗", HttpStatus.UNAUTHORIZED);
        }
    }

    public long getExpirationTime() {
        return jwtConfig.getExpiration() / 1000;
    }

    public void invalidateToken(String token) {
        if (token != null && validateToken(token)) {
            // 在這裡可以添加令牌黑名單的邏輯
            // 由於資料庫中沒有相關表，此處僅作為示例
        }
    }
}
