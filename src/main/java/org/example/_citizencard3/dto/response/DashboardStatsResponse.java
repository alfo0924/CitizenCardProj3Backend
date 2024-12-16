package org.example._citizencard3.dto.response;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@Data
@Builder
public class DashboardStatsResponse {
    private boolean success;
    private String message;
    private String error;

    // 用戶統計
    private long totalUsers;
    private long newUsers;
    private long activeUsers;
    private Map<String, Long> userRoleDistribution;

    // 錢包統計
    private double totalBalance;
    private double averageBalance;

    // 電影統計
    private long activeMovies;
    private long newMovies;
    private Map<String, Long> movieGenreDistribution;

    // 場次統計
    private long totalSchedules;
    private long availableSchedules;

    // 商店統計
    private long totalStores;
    private long newStores;
    private Map<String, Long> storeCategoryDistribution;

    // 電影票統計
    private long totalTickets;
    private long validTickets;
    private long ticketsSoldToday;

    // 優惠券統計
    private long totalCoupons;
    private long activeCoupons;
    private long couponsUsedToday;

    // 系統活躍度
    private long lastLoginCount;

    // 最近活動數據
    private List<Map<String, Object>> recentLogins;
    private List<Map<String, Object>> recentTransactions;
    private List<Map<String, Object>> recentMovieBookings;

    // 時間戳
    private LocalDateTime timestamp;
}
