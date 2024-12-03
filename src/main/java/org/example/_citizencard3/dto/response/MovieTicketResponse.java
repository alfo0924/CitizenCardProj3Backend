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
public class MovieTicketResponse {

    private Long id;
    private String movieTitle;
    private LocalDateTime showTime;
    private String hall;
    private String seatNumber;
    private String status;
    private Double price;

    // 電影相關資訊
    private String posterUrl;
    private String director;
    private String cast;
    private Integer duration;
    private String genre;
    private String rating;

    // 訂單相關資訊
    private String orderNumber;
    private LocalDateTime purchaseTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 票券狀態相關
    private Boolean isValid;
    private Boolean isExpired;
    private Boolean isCancelled;

    // QR Code
    private String qrCodeUrl;

    // 其他資訊
    private String remarks;
}