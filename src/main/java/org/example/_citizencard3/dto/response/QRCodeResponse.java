package org.example._citizencard3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeResponse {

    private Long id;

    // QR碼基本資訊
    private String qrCodeData;
    private String qrCodeUrl;
    private LocalDateTime validUntil;
    private Boolean isUsed;
    private LocalDateTime usedAt;

    // 票券相關資訊
    private String ticketType; // MOVIE_TICKET 或 DISCOUNT_COUPON
    private Long ticketId;
    private String ticketTitle;
    private String ticketStatus;

    // 電影票專屬欄位
    private Long movieId;
    private String movieTitle;
    private Long scheduleId;
    private LocalDateTime showTime;
    private String hall;
    private String seatNumber;

    // 優惠券專屬欄位
    private Long storeId;
    private String storeName;
    private String discountType;
    private Double discountValue;
    private String discountDescription;

    // 時間戳記
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void setType(String ticketType) {
        this.ticketType = ticketType;
    }

    public String getType() {
        return this.ticketType;
    }

    public void setRelatedId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getRelatedId() {
        return this.ticketId;
    }

    // 檢查QR碼是否有效
    public boolean isValid() {
        if (isUsed != null && isUsed) {
            return false;
        }
        return validUntil != null && validUntil.isAfter(LocalDateTime.now());
    }

    // 檢查是否為電影票QR碼
    public boolean isMovieTicket() {
        return "MOVIE_TICKET".equals(ticketType);
    }

    // 檢查是否為優惠券QR碼
    public boolean isDiscountCoupon() {
        return "DISCOUNT_COUPON".equals(ticketType);
    }

    // 取得QR碼狀態描述
    public String getStatusDescription() {
        if (isUsed != null && isUsed) {
            return "已使用";
        }
        if (!isValid()) {
            return "已過期";
        }
        return "有效";
    }
}
