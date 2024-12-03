package org.example._citizencard3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {

    private Long userId;
    private List<MovieTicketResponse> movieTickets;
    private List<DiscountCouponResponse> discountCoupons;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovieTicketResponse {
        private Long id;
        private String movieTitle;
        private String showTime;
        private String hall;
        private String seatNumber;
        private String status;
        private Double price;
        private String qrCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscountCouponResponse {
        private Long id;
        private String title;
        private String description;
        private String discountType;
        private Double discountValue;
        private String status;
        private String expiryDate;
        private String qrCode;
    }
}