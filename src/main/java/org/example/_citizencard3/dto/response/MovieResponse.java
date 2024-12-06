package org.example._citizencard3.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MovieResponse {
    private Long id;
    private String title;
    private String director;
    private String cast;
    private Integer duration;
    private String genre;
    private String rating;
    private String posterUrl;
    private String trailerUrl;
    private LocalDateTime releaseDate;
    private LocalDateTime endDate;
    private Boolean isShowing;
    private Boolean active;
    private Integer price;
    private Double score;
    private String description;

    // 用於場次信息
    private LocalDateTime showTime;
    private String hall;
    private Integer availableSeats;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}