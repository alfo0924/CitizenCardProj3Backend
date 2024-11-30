package org.example._citizencard3.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtConfig {
    // JWT 密鑰
    private String secret = "citycardSecretKey123456789citycardSecretKey123456789";

    // Token 過期時間 (毫秒)
    private long expiration = 86400000; // 24小時

    // Token 前綴
    private String tokenPrefix = "Bearer ";

    // Header 名稱
    private String headerString = "Authorization";

    // 刷新 Token 過期時間 (毫秒)
    private long refreshExpiration = 604800000; // 7天

    // Token 簽發者
    private String issuer = "CitizenCard System";

    // Token 允許的角色
    private String[] allowedRoles = {"ROLE_USER", "ROLE_ADMIN"};

    // Token 黑名單快取時間 (秒)
    private int blacklistCacheExpiry = 3600;

    // Token 最小刷新間隔 (毫秒)
    private long minimumRefreshInterval = 300000; // 5分鐘

    // 是否允許同時登入
    private boolean allowConcurrentLogins = true;

    // 是否在響應中包含刷新 Token
    private boolean includeRefreshToken = true;
}