package org.example._citizencard3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieTicketResponse {
    private Long id;
    private Long userId;
    private String movieTitle;
    private String hall;
    private String seatNumber;
    private String status;
    private LocalDateTime showTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 額外的電影資訊
    private String moviePoster;
    private String movieDirector;
    private String movieCast;
    private Integer movieDuration;
    private String movieGenre;
    private String movieRating;

    // 場次資訊
    private String scheduleHall;
    private Integer totalSeats;
    private Integer availableSeats;

    // 票券狀態相關
    private Boolean isValid;
    private Boolean isUsed;
    private Boolean isExpired;
    private Boolean isCancelled;
}