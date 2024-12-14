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

    // 對應 users 表的欄位
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String birthday;
    private String gender;
    private String role;
    private String address;
    private String avatar;
    private boolean active;
    private boolean emailVerified;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;

    // 對應 wallets 表的欄位
    private Double balance;
}
