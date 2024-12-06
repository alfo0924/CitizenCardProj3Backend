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
}