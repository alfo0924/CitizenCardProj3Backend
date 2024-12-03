package org.example._citizencard3.service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example._citizencard3.dto.response.DiscountCouponResponse;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.model.DiscountCoupon;
import org.example._citizencard3.model.User;  // Add this import
import org.example._citizencard3.repository.DiscountCouponRepository;
import org.example._citizencard3.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscountCouponService {

    private final DiscountCouponRepository discountCouponRepository;
    private final UserRepository userRepository;

    // 獲取當前用戶的優惠券
    public List<DiscountCouponResponse> getCurrentUserCoupons(Long userId) {
        List<DiscountCoupon> coupons = discountCouponRepository.findValidCoupons(
                userId,
                LocalDateTime.now()
        );
        return coupons.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 獲取優惠券詳情
    public DiscountCouponResponse getCouponById(Long couponId) {
        DiscountCoupon coupon = discountCouponRepository.findById(couponId)
                .orElseThrow(() -> new CustomException("優惠券不存在", HttpStatus.NOT_FOUND));
        return convertToResponse(coupon);
    }

    // 使用優惠券
    @Transactional
    public DiscountCouponResponse useCoupon(Long userId, Long couponId) {
        DiscountCoupon coupon = discountCouponRepository.findById(couponId)
                .orElseThrow(() -> new CustomException("優惠券不存在", HttpStatus.NOT_FOUND));

        if (!coupon.getUser().getId().equals(userId)) {
            throw new CustomException("無權使用此優惠券", HttpStatus.FORBIDDEN);
        }

        if (!coupon.isValid()) {
            throw new CustomException("優惠券已失效或過期", HttpStatus.BAD_REQUEST);
        }

        coupon.use();
        discountCouponRepository.save(coupon);
        return convertToResponse(coupon);
    }

    // 檢查優惠券有效性
    public boolean validateCoupon(Long userId, Long couponId) {
        return discountCouponRepository.findById(couponId)
                .map(coupon -> coupon.getUser().getId().equals(userId) && coupon.isValid())
                .orElse(false);
    }

    // 發放優惠券給用戶
    @Transactional
    public DiscountCouponResponse issueCoupon(Long userId, DiscountCoupon.DiscountType type,
                                              Double value, String title, String description, LocalDateTime expiryDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("用戶不存在", HttpStatus.NOT_FOUND));

        DiscountCoupon coupon = DiscountCoupon.builder()
                .user(user)
                .discountType(type)
                .discountValue(value)
                .title(title)
                .description(description)
                .expiryDate(expiryDate)
                .status(DiscountCoupon.CouponStatus.VALID)
                .build();

        coupon = discountCouponRepository.save(coupon);
        return convertToResponse(coupon);
    }

    // 轉換為響應對象
    private DiscountCouponResponse convertToResponse(DiscountCoupon coupon) {
        return DiscountCouponResponse.builder()
                .id(coupon.getId())
                .title(coupon.getTitle())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType().name())
                .discountValue(coupon.getDiscountValue())
                .status(coupon.getStatus().name())
                .expiryDate(coupon.getExpiryDate())
                .isValid(coupon.isValid())
                .isExpired(coupon.isExpired())
                .isUsed(coupon.getStatus() == DiscountCoupon.CouponStatus.USED)
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .build();
    }
}