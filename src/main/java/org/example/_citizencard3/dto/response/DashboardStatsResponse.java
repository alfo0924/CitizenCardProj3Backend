package org.example._citizencard3.dto.response;

import lombok.Data;
import lombok.Builder;

import java.util.Map;

@Data
@Builder
public class DashboardStatsResponse {
    // 用戶統計
    private long totalUsers;
    private long newUsers;
    private Map<String, Long> userRoleDistribution;
    private long activeUsers;
    private boolean success;
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

    // 優惠券統計
    private long totalCoupons;
    private long activeCoupons;

    // 系統活躍度
    private long lastLoginCount;
    private long ticketsSoldToday;
    private long couponsUsedToday;
}
