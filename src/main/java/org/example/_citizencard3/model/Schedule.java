package org.example._citizencard3.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "schedules")
class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(nullable = false)
    private LocalDateTime showTime;  // 放映時間

    @Column(nullable = false)
    private String hall;  // 影廳

    @Column(nullable = false)
    private Integer totalSeats;  // 總座位數

    @Column(nullable = false)
    private Integer availableSeats;  // 可用座位數

    @Column(nullable = false)
    private Boolean active = true;  // 是否啟用

    @Column(nullable = false)
    private LocalDateTime createdAt;  // 創建時間

    @Column(nullable = false)
    private LocalDateTime updatedAt;  // 更新時間

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
