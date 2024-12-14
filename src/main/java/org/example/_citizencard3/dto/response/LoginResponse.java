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

    // 用戶基本資訊
    private Long id;
    private String name;
    private String email;
    private String role;
    private String avatar;

    // 用戶狀態
    private boolean active;
    private boolean emailVerified;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
}
