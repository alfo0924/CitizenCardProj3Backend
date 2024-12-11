package org.example._citizencard3.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movie_tickets")
public class MovieTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "movieTicket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieTicketQRCode> qrCodes;

    public enum TicketStatus {
        VALID,      // 有效
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
            status = TicketStatus.VALID;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 輔助方法
    public boolean isValid() {
        return status == TicketStatus.VALID;
    }

    public boolean isUsed() {
        return status == TicketStatus.USED;
    }

    public boolean isExpired() {
        return status == TicketStatus.EXPIRED;
    }

    public boolean isCancelled() {
        return status == TicketStatus.CANCELLED;
    }

    public void markAsUsed() {
        this.status = TicketStatus.USED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsExpired() {
        this.status = TicketStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = TicketStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
}