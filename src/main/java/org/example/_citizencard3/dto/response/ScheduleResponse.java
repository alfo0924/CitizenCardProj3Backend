package org.example._citizencard3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {

    private Long id;

    private Long movieId;

    private String movieTitle;

    private LocalDateTime showTime;

    private String hall;

    private Integer totalSeats;

    private Integer availableSeats;

    private Boolean active;

    // 額外資訊
    private String posterUrl;

    private String rating;

    private Integer duration;

    private Double score;

    // 場次狀態
    private Boolean isSoldOut;

    private Boolean isStarted;

    private Boolean isEnded;

    // 座位資訊
    private Integer occupiedSeats;

    private Double occupancyRate;

    // 時間資訊
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 計算座位相關資訊
    public void calculateSeatsInfo() {
        this.occupiedSeats = this.totalSeats - this.availableSeats;
        this.occupancyRate = (double) this.occupiedSeats / this.totalSeats;
        this.isSoldOut = this.availableSeats == 0;
    }

    // 計算場次狀態
    public void calculateShowStatus() {
        LocalDateTime now = LocalDateTime.now();
        this.isStarted = now.isAfter(this.showTime);
        this.isEnded = now.isAfter(this.showTime.plusMinutes(this.duration));
    }
}
