package org.example._citizencard3.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeGenerateRequest {

    @NotNull(message = "票券ID不能為空")
    private Long ticketId;

    @NotNull(message = "票券類型不能為空")
    private String ticketType; // MOVIE_TICKET 或 DISCOUNT_COUPON

    @NotNull(message = "有效期限不能為空")
    private LocalDateTime validUntil;

    private String customData; // 自定義額外資料

    // 用於電影票
    private Long movieId;
    private Long scheduleId;
    private String seatNumber;

    // 用於優惠券
    private Long storeId;
    private String discountType;
    private Double discountValue;
}
