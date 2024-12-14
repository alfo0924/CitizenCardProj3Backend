package org.example._citizencard3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    // JWT相關欄位
    private String token;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;

    // 基本用戶資訊
    private Long id;
    private String email;
    private String name;
    private String role;
    private String avatar;
    private boolean emailVerified;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
}
