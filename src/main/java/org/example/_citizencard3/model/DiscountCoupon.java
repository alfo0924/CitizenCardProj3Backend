package org.example._citizencard3.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "discount_coupons")
public class DiscountCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "discount_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private Double discountValue;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CouponStatus status = CouponStatus.VALID;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum DiscountType {
        PERCENTAGE,    // 百分比折扣
        FIXED_AMOUNT,  // 固定金額折扣
        FREE_TICKET    // 免費票券
    }

    public enum CouponStatus {
        VALID,     // 可使用
        USED,      // 已使用
        EXPIRED,   // 已過期
        CANCELLED  // 已取消
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 檢查優惠券是否過期
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    // 檢查優惠券是否可用
    public boolean isValid() {
        return status == CouponStatus.VALID && !isExpired();
    }
}