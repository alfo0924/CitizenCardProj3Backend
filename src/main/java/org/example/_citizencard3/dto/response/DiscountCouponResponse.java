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
    private String discountType;  // PERCENTAGE or FIXED_AMOUNT
    private Double discountValue;
    private String status;        // VALID, USED, EXPIRED
    private LocalDateTime expiryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}