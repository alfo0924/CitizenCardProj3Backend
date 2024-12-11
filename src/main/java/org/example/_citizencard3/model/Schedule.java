package org.example._citizencard3.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "schedules")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "電影不能為空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @NotNull(message = "放映時間不能為空")
    @Column(nullable = false)
    private LocalDateTime showTime;

    @NotNull(message = "影廳不能為空")
    @Column(nullable = false)
    private String hall;

    @NotNull(message = "可用座位數不能為空")
    @Min(value = 0, message = "可用座位數不能小於0")
    @Column(nullable = false)
    private Integer availableSeats;

    @NotNull(message = "總座位數不能為空")
    @Min(value = 1, message = "總座位數必須大於0")
    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @NotNull(message = "狀態不能為空")
    @Column(nullable = false)
    private Boolean active = true;

    @NotNull(message = "創建時間不能為空")
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @NotNull(message = "更新時間不能為空")
    @Column(nullable = false)
    private LocalDateTime updatedAt;

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

    public Boolean isActive() {
        return this.active;
    }

    public Long getMovieId() {
        return movie != null ? movie.getId() : null;
    }

    public void setMovieId(@NotNull(message = "電影ID不能為空") Long movieId) {
        if (this.movie == null) {
            this.movie = new Movie();
        }
        this.movie.setId(movieId);
    }

    // 座位相關方法
    public boolean hasAvailableSeats() {
        return availableSeats > 0;
    }

    public boolean canBook(int requestedSeats) {
        if (requestedSeats <= 0) {
            throw new IllegalArgumentException("訂票數量必須大於0");
        }
        return availableSeats >= requestedSeats;
    }

    public void bookSeats(int seats) {
        if (!canBook(seats)) {
            throw new IllegalStateException("座位數量不足");
        }
        if (!isActive()) {
            throw new IllegalStateException("該場次已關閉");
        }
        if (isExpired()) {
            throw new IllegalStateException("該場次已過期");
        }
        this.availableSeats -= seats;
    }

    public void cancelBooking(int seats) {
        if (seats <= 0) {
            throw new IllegalArgumentException("取消座位數必須大於0");
        }
        if (this.availableSeats + seats > this.totalSeats) {
            throw new IllegalStateException("超出總座位數限制");
        }
        this.availableSeats += seats;
    }

    // 狀態檢查方法
    public boolean isShowTimeValid() {
        return showTime != null && showTime.isAfter(LocalDateTime.now());
    }

    public boolean isExpired() {
        return showTime != null && showTime.isBefore(LocalDateTime.now());
    }

    public boolean isFull() {
        return availableSeats != null && availableSeats == 0;
    }

    // 輔助方法
    public double getOccupancyRate() {
        if (totalSeats == null || totalSeats == 0) {
            return 0.0;
        }
        return (totalSeats - availableSeats) * 100.0 / totalSeats;
    }

    public boolean isInTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (showTime == null || startTime == null || endTime == null) {
            return false;
        }
        return !showTime.isBefore(startTime) && !showTime.isAfter(endTime);
    }

    public boolean isInSameHall(String otherHall) {
        if (hall == null || otherHall == null) {
            return false;
        }
        return this.hall.equals(otherHall);
    }
}
