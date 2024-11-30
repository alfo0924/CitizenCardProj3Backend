package org.example._citizencard3.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stores")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private String address;

    private String phone;

    private String email;

    private String website;

    private String openingHours;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "discount_info", length = 500)
    private String discountInfo;

    private Double latitude;

    private Double longitude;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    private Integer version;

    // 軟刪除標記
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    // 評分相關
    private Double rating;

    @Column(name = "rating_count")
    private Integer ratingCount;

    // 合作狀態
    @Enumerated(EnumType.STRING)
    @Column(name = "partnership_status")
    private PartnershipStatus partnershipStatus;

    public enum PartnershipStatus {
        ACTIVE,
        PENDING,
        SUSPENDED,
        TERMINATED
    }

    // 業務方法
    public void updateRating(Double newRating) {
        if (this.ratingCount == null) this.ratingCount = 0;
        if (this.rating == null) this.rating = 0.0;

        this.rating = ((this.rating * this.ratingCount) + newRating) / (this.ratingCount + 1);
        this.ratingCount++;
    }

    public void softDelete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
    }

    public void suspend() {
        this.active = false;
        this.partnershipStatus = PartnershipStatus.SUSPENDED;
    }

    public void activate() {
        this.active = true;
        this.partnershipStatus = PartnershipStatus.ACTIVE;
    }
}