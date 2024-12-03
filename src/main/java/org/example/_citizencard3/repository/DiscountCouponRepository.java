package org.example._citizencard3.repository;

import org.example._citizencard3.model.DiscountCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DiscountCouponRepository extends JpaRepository<DiscountCoupon, Long> {

    // 根據用戶ID查找優惠券
    List<DiscountCoupon> findByUserIdAndStatus(Long userId, DiscountCoupon.CouponStatus status);


    // 查找用戶的有效優惠券
    @Query("SELECT d FROM DiscountCoupon d WHERE d.user.id = :userId " +
            "AND d.status = 'VALID' AND d.expiryDate > :now")
    List<DiscountCoupon> findValidCoupons(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // 查找即將過期的優惠券
    @Query("SELECT d FROM DiscountCoupon d WHERE d.user.id = :userId " +
            "AND d.status = 'VALID' AND d.expiryDate BETWEEN :now AND :expiry")
    List<DiscountCoupon> findExpiringCoupons(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now,
            @Param("expiry") LocalDateTime expiry
    );

    // 根據折扣類型查找優惠券
    List<DiscountCoupon> findByUserIdAndDiscountType(Long userId, DiscountCoupon.DiscountType discountType);

    // 查找用戶所有優惠券
    List<DiscountCoupon> findByUserId(Long userId);

    // 檢查優惠券是否存在
    boolean existsByUserIdAndId(Long userId, Long couponId);

    // 更新優惠券狀態
    @Query("UPDATE DiscountCoupon d SET d.status = :status WHERE d.id = :couponId")
    void updateStatus(@Param("couponId") Long couponId, @Param("status") DiscountCoupon.CouponStatus status);

    // 刪除過期優惠券
    @Query("DELETE FROM DiscountCoupon d WHERE d.expiryDate < :now AND d.status = 'VALID'")
    void deleteExpiredCoupons(@Param("now") LocalDateTime now);

    // 統計用戶優惠券數量
    long countByUserIdAndStatus(Long userId, DiscountCoupon.CouponStatus status);

    // 查找特定類型的優惠券
    @Query("SELECT d FROM DiscountCoupon d WHERE d.discountType = :type " +
            "AND d.status = 'VALID' AND d.expiryDate > :now")
    List<DiscountCoupon> findByDiscountType(
            @Param("type") DiscountCoupon.DiscountType type,
            @Param("now") LocalDateTime now
    );

    // 查找指定折扣值範圍的優惠券
    @Query("SELECT d FROM DiscountCoupon d WHERE d.discountValue BETWEEN :minValue AND :maxValue " +
            "AND d.status = 'VALID'")
    List<DiscountCoupon> findByDiscountValueRange(
            @Param("minValue") Double minValue,
            @Param("maxValue") Double maxValue
    );

}