package org.example._citizencard3.controller;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.service.MovieService;
import org.example._citizencard3.service.StoreService;
import org.example._citizencard3.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private final UserService userService;
    private final MovieService movieService;
    private final StoreService storeService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> dashboardData = new HashMap<>();
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

            // 用戶統計
            Map<String, Object> userStats = new HashMap<>();
            long totalUsers = userService.countAllUsers();
            long newUsers = userService.countNewUsersAfter(oneMonthAgo);
            userStats.put("totalUsers", totalUsers);
            userStats.put("newUsers", newUsers);

            // 電影統計
            Map<String, Object> movieStats = new HashMap<>();
            long activeMovies = movieService.countActiveMovies();
            long newMovies = movieService.countNewMoviesAfter(oneMonthAgo);
            movieStats.put("activeMovies", activeMovies);
            movieStats.put("newMovies", newMovies);

            // 商店統計
            Map<String, Object> storeStats = new HashMap<>();
            long totalStores = storeService.countActiveStores();
            long newStores = storeService.countNewStoresAfter(oneMonthAgo);
            storeStats.put("totalStores", totalStores);
            storeStats.put("newStores", newStores);

            // 會員分析數據
            Map<String, Object> userAnalytics = new HashMap<>();
            Map<String, Long> userRoleDistribution = userService.getUserRoleDistribution();
            userAnalytics.put("labels", userRoleDistribution.keySet());
            userAnalytics.put("data", userRoleDistribution.values());

            // 商店類型分析
            Map<String, Object> storeAnalytics = new HashMap<>();
            Map<String, Long> storeCategoryDistribution = storeService.getStoreCategoryDistribution();
            storeAnalytics.put("labels", storeCategoryDistribution.keySet());
            storeAnalytics.put("data", storeCategoryDistribution.values());

            // 組合所有數據
            dashboardData.put("stats", Map.of(
                    "totalUsers", totalUsers,
                    "newUsers", newUsers,
                    "totalStores", totalStores,
                    "newStores", newStores,
                    "activeMovies", activeMovies,
                    "newMovies", newMovies
            ));
            dashboardData.put("userData", userAnalytics);
            dashboardData.put("storeData", storeAnalytics);
            dashboardData.put("success", true);

            return ResponseEntity.ok(dashboardData);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "獲取儀表板數據失敗");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> checkSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("success", true);
        status.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(status);
    }
}
