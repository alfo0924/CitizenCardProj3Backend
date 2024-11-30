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
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;  // 電影標題

    @Column(length = 2000)
    private String description;  // 電影描述

    @Column(nullable = false)
    private String director;  // 導演

    @Column(nullable = false)
    private String cast;  // 演員陣容

    @Column(nullable = false)
    private Integer duration;  // 片長(分鐘)

    @Column(nullable = false)
    private String genre;  // 電影類型

    @Column(nullable = false)
    private String rating;  // 分級

    private String posterUrl;  // 海報URL

    private String trailerUrl;  // 預告片URL

    @Column(nullable = false)
    private LocalDateTime releaseDate;  // 上映日期

    @Column(nullable = false)
    private LocalDateTime endDate;  // 下檔日期

    @Column(nullable = false)
    private Boolean isShowing;  // 是否上映中

    private Double score;  // 評分

    @Column(nullable = false)
    private Integer price;  // 票價

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<Schedule> schedules;  // 場次列表

    @Column(nullable = false)
    private Boolean active = true;  // 是否啟用

    @Column(nullable = false)
    private LocalDateTime createdAt;  // 創建時間

    @Column(nullable = false)
    private LocalDateTime updatedAt;  // 更新時間

    // 更新時間的自動處理
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