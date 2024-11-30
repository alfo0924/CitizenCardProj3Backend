package org.example._citizencard3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;              // JWT 訪問令牌
    private String refreshToken;       // 刷新令牌
    private String tokenType;          // 令牌類型，通常是 "Bearer"
    private long expiresIn;           // 令牌過期時間（秒）

    private Long userId;              // 用戶ID
    private String email;             // 用戶郵箱
    private String name;              // 用戶名稱
    private String role;              // 用戶角色
    private String avatar;            // 用戶頭像URL

    // 額外的用戶信息
    private boolean isEmailVerified;  // 郵箱是否驗證
    private String lastLoginTime;     // 最後登入時間
    private String lastLoginIp;       // 最後登入IP
}