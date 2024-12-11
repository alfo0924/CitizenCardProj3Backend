package org.example._citizencard3.repository;

import org.example._citizencard3.model.DiscountCouponQRCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountCouponQRCodeRepository extends JpaRepository<DiscountCouponQRCode, Long> {

    // 根據優惠券ID查詢QR碼
    Optional<DiscountCouponQRCode> findByCouponId(Long couponId);

    // 根據QR碼數據查詢
    Optional<DiscountCouponQRCode> findByQrCodeData(String qrCodeData);

    // 查詢指定用戶的所有有效QR碼
    @Query("SELECT q FROM DiscountCouponQRCode q JOIN DiscountCoupon c ON q.couponId = c.id " +
            "WHERE c.userId = :userId AND q.validUntil > :now AND q.isUsed = false")
    List<DiscountCouponQRCode> findValidQRCodesByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // 查詢即將過期的QR碼(7天內)
    @Query("SELECT qr FROM DiscountCouponQRCode qr " +
            "WHERE qr.validUntil BETWEEN :now AND :sevenDaysLater " +
            "AND qr.isUsed = false")
    List<DiscountCouponQRCode> findExpiringQRCodes(
            @Param("now") LocalDateTime now,
            @Param("sevenDaysLater") LocalDateTime sevenDaysLater
    );

    // 查詢已過期的QR碼
    List<DiscountCouponQRCode> findByValidUntilBeforeAndIsUsedFalse(LocalDateTime now);

    // 查詢已使用的QR碼
    List<DiscountCouponQRCode> findByIsUsedTrue();

    // 根據商店ID查詢相關的QR碼
    @Query("SELECT qr FROM DiscountCouponQRCode qr " +
            "JOIN DiscountCoupon dc ON qr.couponId = dc.id " +
            "JOIN Store s ON dc.id = s.id " +
            "WHERE s.id = :storeId")
    List<DiscountCouponQRCode> findByStoreId(@Param("storeId") Long storeId);

    // 檢查QR碼是否有效
    @Query("SELECT CASE WHEN COUNT(qr) > 0 THEN true ELSE false END " +
            "FROM DiscountCouponQRCode qr " +
            "WHERE qr.qrCodeData = :qrCodeData " +
            "AND qr.validUntil > :now " +
            "AND qr.isUsed = false")
    boolean isQRCodeValid(
            @Param("qrCodeData") String qrCodeData,
            @Param("now") LocalDateTime now
    );

    // 更新QR碼使用狀態
    @Modifying
    @Transactional
    @Query("UPDATE DiscountCouponQRCode qr " +
            "SET qr.isUsed = true, qr.usedAt = :usedAt " +
            "WHERE qr.id = :id")
    void updateQRCodeUsed(
            @Param("id") Long id,
            @Param("usedAt") LocalDateTime usedAt
    );

    // 刪除過期的QR碼
    @Modifying
    @Transactional
    void deleteByValidUntilBefore(LocalDateTime dateTime);
}
