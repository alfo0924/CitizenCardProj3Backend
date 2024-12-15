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
@Table(name = "movie_ticket_qrcodes")
public class MovieTicketQRCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "qr_code_data", nullable = false, length = 255)
    private String qrCodeData;

    @Column(name = "qr_code_url", nullable = false, length = 255)
    private String qrCodeUrl;

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", referencedColumnName = "id", insertable = false, updatable = false)
    private MovieTicket movieTicket;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 輔助方法
    public boolean isValid() {
        return !isUsed && validUntil.isAfter(LocalDateTime.now());
    }

    public boolean isExpired() {
        return validUntil.isBefore(LocalDateTime.now());
    }

    public void markAsUsed() {
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
    }
}
