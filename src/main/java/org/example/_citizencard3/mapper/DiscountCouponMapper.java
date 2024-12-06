package org.example._citizencard3.mapper;

import org.example._citizencard3.dto.response.DiscountCouponResponse;
import org.example._citizencard3.model.DiscountCoupon;
import org.springframework.stereotype.Component;

@Component
public class DiscountCouponMapper {

    public DiscountCouponResponse toResponse(DiscountCoupon coupon) {
        return DiscountCouponResponse.builder()
                .id(coupon.getId())
                .title(coupon.getTitle())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType().toString())
                .discountValue(coupon.getDiscountValue())
                .status(coupon.getStatus().toString())
                .expiryDate(coupon.getExpiryDate())
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .build();
    }

    public DiscountCoupon toEntity(DiscountCouponResponse response) {
        return DiscountCoupon.builder()
                .id(response.getId())
                .title(response.getTitle())
                .description(response.getDescription())
                .discountType(DiscountCoupon.DiscountType.valueOf(response.getDiscountType()))
                .discountValue(response.getDiscountValue())
                .status(DiscountCoupon.CouponStatus.valueOf(response.getStatus()))
                .expiryDate(response.getExpiryDate())
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .build();
    }
}