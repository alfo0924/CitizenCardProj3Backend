package org.example._citizencard3.controller;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.service.MovieService;
import org.example._citizencard3.service.StoreService;
import org.example._citizencard3.service.UserService;
import org.example._citizencard3.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}",
        allowCredentials = "true",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class SystemController {

    private final UserService userService;
    private final MovieService movieService;
    private final StoreService storeService;
    private final WalletService walletService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> dashboardData = new HashMap<>();
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

            // 用戶統計
            long totalUsers = userService.countAllUsers();
            long newUsers = userService.countNewUsersAfter(oneMonthAgo);
            long activeUsers = userService.countByLastLoginTimeAfter(oneMonthAgo);

            // 電影統計
            long activeMovies = movieService.countActiveMovies();
            long newMovies = movieService.countNewMoviesAfter(oneMonthAgo);

            // 商店統計
            long totalStores = storeService.countActiveStores();
            long newStores = storeService.countNewStoresAfter(oneMonthAgo);

            // 錢包統計
            double totalBalance = walletService.sumBalance();
            double averageBalance = walletService.averageBalance();

            // 角色分佈統計
            Map<String, Long> userRoleDistribution = userService.getUserRoleDistribution();

            // 商店類別分佈統計
            Map<String, Long> storeCategoryDistribution = storeService.getStoreCategoryDistribution();

            // 組合統計數據
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("newUsers", newUsers);
            stats.put("activeUsers", activeUsers);
            stats.put("totalStores", totalStores);
            stats.put("newStores", newStores);
            stats.put("activeMovies", activeMovies);
            stats.put("newMovies", newMovies);
            stats.put("totalBalance", totalBalance);
            stats.put("averageBalance", averageBalance);

            // 組合圖表數據
            Map<String, Object> userAnalytics = new HashMap<>();
            userAnalytics.put("labels", userRoleDistribution.keySet());
            userAnalytics.put("data", userRoleDistribution.values());

            Map<String, Object> storeAnalytics = new HashMap<>();
            storeAnalytics.put("labels", storeCategoryDistribution.keySet());
            storeAnalytics.put("data", storeCategoryDistribution.values());

            // 組合最終響應
            dashboardData.put("stats", stats);
            dashboardData.put("userRoleDistribution", userAnalytics);
            dashboardData.put("storeCategoryDistribution", storeAnalytics);
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
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("success", true);
            status.put("timestamp", LocalDateTime.now());
            status.put("service", "running");
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("success", false);
            errorStatus.put("message", "系統狀態檢查失敗");
            return ResponseEntity.internalServerError().body(errorStatus);
        }
    }
}
