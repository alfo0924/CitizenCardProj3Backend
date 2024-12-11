package org.example._citizencard3.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "discount_coupons")
public class DiscountCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Column(name = "store_id", insertable = false, updatable = false)
    private Long storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "discount_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false)
    private Double discountValue;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CouponStatus status;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "discountCoupon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiscountCouponQRCode> qrCodes = new ArrayList<>();

    public enum DiscountType {
        PERCENTAGE,    // 百分比折扣
        FIXED_AMOUNT   // 固定金額折扣
    }

    public enum CouponStatus {
        VALID,      // 可使用
        USED,       // 已使用
        EXPIRED,    // 已過期
        CANCELLED   // 已取消
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = CouponStatus.VALID;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (expiryDate != null && expiryDate.isBefore(LocalDateTime.now())) {
            status = CouponStatus.EXPIRED;
        }
    }

    public boolean isValid() {
        return status == CouponStatus.VALID &&
                expiryDate != null &&
                expiryDate.isAfter(LocalDateTime.now());
    }

    public boolean canUse() {
        return isValid() && !isExpired();
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now());
    }

    // 優惠券管理方法
    public void markAsUsed() {
        this.status = CouponStatus.USED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = CouponStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    // QR碼管理方法
    public void addQRCode(DiscountCouponQRCode qrCode) {
        qrCodes.add(qrCode);
        qrCode.setDiscountCoupon(this);
    }

    public void removeQRCode(DiscountCouponQRCode qrCode) {
        qrCodes.remove(qrCode);
        qrCode.setDiscountCoupon(null);
    }
}
