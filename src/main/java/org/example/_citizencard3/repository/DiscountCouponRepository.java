package org.example._citizencard3.repository;

import org.example._citizencard3.model.DiscountCoupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DiscountCouponRepository extends JpaRepository<DiscountCoupon, Long> {

    Page<DiscountCoupon> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<DiscountCoupon> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status, Pageable pageable);

    List<DiscountCoupon> findByStatusAndExpiryDateBefore(String status, LocalDateTime date);

    List<DiscountCoupon> findByUserIdAndStatus(Long userId, String status);

    boolean existsByUserIdAndStatus(Long userId, String status);

    long countByUserIdAndStatus(Long userId, String status);

    /**
     * 計算指定狀態的優惠券數量
     * @param status 優惠券狀態
     * @return 符合指定狀態的優惠券數量
     */
    long countByStatusEquals(String status);

    /**
     * 計算在指定日期之後更新且具有指定狀態的優惠券數量
     * @param status 優惠券狀態
     * @param dateTime 指定的日期時間
     * @return 符合條件的優惠券數量
     */
    long countByStatusEqualsAndUpdatedAtAfter(String status, LocalDateTime dateTime);

}