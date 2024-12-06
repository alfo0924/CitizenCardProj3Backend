package org.example._citizencard3.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtConfig {
    // JWT 密鑰 (至少256位)
    private String secret = "citycardSecretKey123456789citycardSecretKey123456789";

    // 訪問Token過期時間 (24小時)
    private long expiration = 86400000;

    // 刷新Token過期時間 (7天)
    private long refreshExpiration = 604800000;

    // Token前綴
    private String tokenPrefix = "Bearer ";

    // HTTP請求頭名稱
    private String headerString = "Authorization";

    // Token簽發者
    private String issuer = "CitizenCard System";

    // 允許的角色
    private String[] allowedRoles = {
            "ROLE_USER",
            "ROLE_ADMIN"
    };

    // Token黑名單快取時間(秒)
    private int blacklistCacheExpiry = 3600;

    // Token最小刷新間隔(5分鐘)
    private long minimumRefreshInterval = 300000;

    // 是否允許同時登入多個設備
    private boolean allowConcurrentLogins = true;

    // 是否在登入響應中包含刷新Token
    private boolean includeRefreshToken = true;

    // 是否驗證Token簽發者
    private boolean validateIssuer = true;

    // 是否驗證Token接收者
    private boolean validateAudience = false;

    // Token接收者
    private String audience = "CitizenCard Client";

    // Token簽名算法
    private String signatureAlgorithm = "HS512";

    // 是否在Token中包含用戶角色
    private boolean includeAuthorities = true;

    // 是否在Token中包含用戶ID
    private boolean includeUserId = true;

    // Token claims key配置
    private String userIdKey = "uid";
    private String usernameKey = "sub";
    private String authoritiesKey = "roles";
    private String tokenTypeKey = "type";
}