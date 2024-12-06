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
public class WalletInfoResponse {
    private Long id;
    private Long userId;
    private String cardNumber;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 票券統計
    private Integer validTicketsCount;
    private Integer validCouponsCount;

    // 卡片狀態
    private Boolean isActive;
    private Boolean isLocked;
    private LocalDateTime lastUsedAt;

    // 用戶資訊
    private String userName;
    private String userEmail;
    private String userPhone;
}