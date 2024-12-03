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
public class DiscountCouponResponse {

    private Long id;
    private String title;
    private String description;
    private String discountType;
    private Double discountValue;
    private String status;
    private LocalDateTime expiryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 額外資訊
    private String storeInfo;
    private String usageConditions;
    private String usageInstructions;
    private Integer usageLimit;
    private Integer usageCount;

    // 狀態標記
    private Boolean isValid;
    private Boolean isExpired;
    private Boolean isUsed;

    // QR Code
    private String qrCodeUrl;

    // 使用記錄
    private LocalDateTime usedAt;
    private String usedLocation;
    private String transactionId;
}