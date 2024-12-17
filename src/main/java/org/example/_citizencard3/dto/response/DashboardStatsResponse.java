package org.example._citizencard3.dto.response;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    // 響應狀態
    @NotNull(message = "成功狀態不能為空")
    private boolean success;
    private String message;
    private String error;

    // 用戶統計
    @PositiveOrZero(message = "總用戶數不能為負數")
    private long totalUsers;

    @PositiveOrZero(message = "新用戶數不能為負數")
    private long newUsers;

    @PositiveOrZero(message = "活躍用戶數不能為負數")
    private long activeUsers;

    @Builder.Default
    private Map<String, Long> userRoleDistribution = new HashMap<>();

    // 錢包統計
    @PositiveOrZero(message = "總餘額不能為負數")
    private double totalBalance;

    @PositiveOrZero(message = "平均餘額不能為負數")
    private double averageBalance;

    // 電影統計
    @PositiveOrZero(message = "上映電影數不能為負數")
    private long activeMovies;

    @PositiveOrZero(message = "新電影數不能為負數")
    private long newMovies;

    @Builder.Default
    private Map<String, Long> movieGenreDistribution = new HashMap<>();

    // 場次統計
    @PositiveOrZero(message = "總場次數不能為負數")
    private long totalSchedules;

    @PositiveOrZero(message = "可用場次數不能為負數")
    private long availableSchedules;

    // 商店統計
    @PositiveOrZero(message = "總商店數不能為負數")
    private long totalStores;

    @PositiveOrZero(message = "新商店數不能為負數")
    private long newStores;

    @Builder.Default
    private Map<String, Long> storeCategoryDistribution = new HashMap<>();

    // 電影票統計
    @PositiveOrZero(message = "總票券數不能為負數")
    private long totalTickets;

    @PositiveOrZero(message = "有效票券數不能為負數")
    private long validTickets;

    @PositiveOrZero(message = "今日售票數不能為負數")
    private long ticketsSoldToday;

    // 優惠券統計
    @PositiveOrZero(message = "總優惠券數不能為負數")
    private long totalCoupons;

    @PositiveOrZero(message = "有效優惠券數不能為負數")
    private long activeCoupons;

    @PositiveOrZero(message = "今日使用優惠券數不能為負數")
    private long couponsUsedToday;

    // 系統活躍度
    @PositiveOrZero(message = "登入次數不能為負數")
    private long lastLoginCount;

    // 最近活動數據
    @Builder.Default
    private List<Map<String, Object>> recentLogins = new ArrayList<>();

    @Builder.Default
    private List<Map<String, Object>> recentTransactions = new ArrayList<>();

    @Builder.Default
    private List<Map<String, Object>> recentMovieBookings = new ArrayList<>();

    // 時間戳
    @NotNull(message = "時間戳不能為空")
    private LocalDateTime timestamp;

    // 自定義驗證方法
    public boolean isValid() {
        return timestamp != null &&
                userRoleDistribution != null &&
                movieGenreDistribution != null &&
                storeCategoryDistribution != null &&
                recentLogins != null &&
                recentTransactions != null &&
                recentMovieBookings != null &&
                totalUsers >= 0 &&
                newUsers >= 0 &&
                activeUsers >= 0 &&
                totalBalance >= 0 &&
                averageBalance >= 0 &&
                activeMovies >= 0 &&
                newMovies >= 0 &&
                totalStores >= 0 &&
                newStores >= 0 &&
                totalTickets >= 0 &&
                validTickets >= 0 &&
                ticketsSoldToday >= 0 &&
                totalCoupons >= 0 &&
                activeCoupons >= 0 &&
                couponsUsedToday >= 0 &&
                lastLoginCount >= 0;
    }

    // 獲取錯誤信息
    public String getValidationErrors() {
        List<String> errors = new ArrayList<>();

        if (timestamp == null) errors.add("時間戳不能為空");
        if (userRoleDistribution == null) errors.add("用戶角色分佈不能為空");
        if (movieGenreDistribution == null) errors.add("電影類型分佈不能為空");
        if (storeCategoryDistribution == null) errors.add("商店類別分佈不能為空");
        if (recentLogins == null) errors.add("最近登入記錄不能為空");
        if (recentTransactions == null) errors.add("最近交易記錄不能為空");
        if (recentMovieBookings == null) errors.add("最近訂票記錄不能為空");

        if (totalUsers < 0) errors.add("總用戶數不能為負數");
        if (newUsers < 0) errors.add("新用戶數不能為負數");
        if (activeUsers < 0) errors.add("活躍用戶數不能為負數");
        if (totalBalance < 0) errors.add("總餘額不能為負數");
        if (averageBalance < 0) errors.add("平均餘額不能為負數");
        if (activeMovies < 0) errors.add("上映電影數不能為負數");
        if (newMovies < 0) errors.add("新電影數不能為負數");
        if (totalStores < 0) errors.add("總商店數不能為負數");
        if (newStores < 0) errors.add("新商店數不能為負數");
        if (totalTickets < 0) errors.add("總票券數不能為負數");
        if (validTickets < 0) errors.add("有效票券數不能為負數");
        if (ticketsSoldToday < 0) errors.add("今日售票數不能為負數");
        if (totalCoupons < 0) errors.add("總優惠券數不能為負數");
        if (activeCoupons < 0) errors.add("有效優惠券數不能為負數");
        if (couponsUsedToday < 0) errors.add("今日使用優惠券數不能為負數");
        if (lastLoginCount < 0) errors.add("登入次數不能為負數");

        return String.join("、", errors);
    }
}
