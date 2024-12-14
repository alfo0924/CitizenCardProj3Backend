package org.example._citizencard3.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    // JWT相關欄位
    private String token;

    // 用戶基本資訊 (對應 users 表)
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
    @Getter
    private String tokenType;

    @Getter
    private long expiresIn;

    // 錢包資訊 (對應 wallets 表)
    private WalletInfo wallet;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalletInfo {
        private Long id;
        private Double balance;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }    public LoginResponse tokenType(String tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    public LoginResponse expiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
        return this;
    }

}
