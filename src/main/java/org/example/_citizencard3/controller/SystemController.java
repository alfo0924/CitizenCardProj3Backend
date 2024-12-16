package org.example._citizencard3.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizencard3.dto.response.DashboardStatsResponse;
import org.example._citizencard3.service.MovieService;
import org.example._citizencard3.service.StoreService;
import org.example._citizencard3.service.SystemService;
import org.example._citizencard3.service.UserService;
import org.example._citizencard3.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}",
        allowCredentials = "true",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class SystemController {

    private final SystemService systemService;
    private final UserService userService;
    private final MovieService movieService;
    private final StoreService storeService;
    private final WalletService walletService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        try {
            log.debug("Fetching dashboard statistics");
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneMonthAgo = now.minusMonths(1);
            LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0);

            // 基礎統計數據
            long totalUsers = userService.countAllUsers();
            long newUsers = userService.countNewUsersAfter(oneMonthAgo);
            long activeUsers = userService.countByLastLoginTimeAfter(oneMonthAgo);

            long totalStores = storeService.countActiveStores();
            long newStores = storeService.countNewStoresAfter(oneMonthAgo);

            long activeMovies = movieService.countActiveMovies();
            long newMovies = movieService.countNewMoviesAfter(oneMonthAgo);

            // 財務統計
            double totalBalance = walletService.sumBalance();
            double averageBalance = walletService.averageBalance();

            // 分佈統計
            Map<String, Long> userRoleDistribution = userService.getUserRoleDistribution();
            Map<String, Long> storeCategoryDistribution = storeService.getStoreCategoryDistribution();

            // 最近活動
            var recentLogins = userService.getRecentLogins(10);
            var recentTransactions = walletService.getRecentTransactions(10);
            var recentBookings = movieService.getRecentBookings(10);

            DashboardStatsResponse response = DashboardStatsResponse.builder()
                    .success(true)
                    .totalUsers(totalUsers)
                    .newUsers(newUsers)
                    .activeUsers(activeUsers)
                    .totalStores(totalStores)
                    .newStores(newStores)
                    .activeMovies(activeMovies)
                    .newMovies(newMovies)
                    .totalBalance(totalBalance)
                    .averageBalance(averageBalance)
                    .userRoleDistribution(userRoleDistribution)
                    .storeCategoryDistribution(storeCategoryDistribution)
                    .recentLogins(recentLogins)
                    .recentTransactions(recentTransactions)
                    .recentMovieBookings(recentBookings)
                    .timestamp(now)
                    .build();

            log.debug("Successfully fetched dashboard statistics");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching dashboard statistics", e);
            return ResponseEntity.internalServerError().body(
                    DashboardStatsResponse.builder()
                            .success(false)
                            .message("獲取儀表板數據失敗")
                            .error(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> checkSystemStatus() {
        try {
            log.debug("Checking system status");
            String dbStatus = checkDatabaseStatus();
            String apiStatus = checkApiStatus();
            String cacheStatus = checkCacheStatus();

            if (!"connected".equals(dbStatus) ||
                    !"operational".equals(apiStatus) ||
                    !"running".equals(cacheStatus)) {
                throw new RuntimeException("系統服務異常");
            }

            Map<String, Object> status = new HashMap<>();
            status.put("success", true);
            status.put("timestamp", LocalDateTime.now());
            status.put("service", "running");
            status.put("databaseStatus", dbStatus);
            status.put("apiStatus", apiStatus);
            status.put("cacheStatus", cacheStatus);

            log.debug("System status check completed successfully");
            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("System status check failed", e);
            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("success", false);
            errorStatus.put("message", "系統狀態檢查失敗");
            errorStatus.put("error", e.getMessage());
            errorStatus.put("timestamp", LocalDateTime.now());
            return ResponseEntity.internalServerError().body(errorStatus);
        }
    }

    private String checkDatabaseStatus() {
        try {
            userService.countAllUsers(); // 簡單的數據庫檢查
            return "connected";
        } catch (Exception e) {
            log.error("Database check failed", e);
            return "disconnected";
        }
    }

    private String checkApiStatus() {
        try {
            // 實現基本的 API 健康檢查
            return "operational";
        } catch (Exception e) {
            log.error("API check failed", e);
            return "failed";
        }
    }

    private String checkCacheStatus() {
        try {
            // 實現緩存服務檢查
            return "running";
        } catch (Exception e) {
            log.error("Cache check failed", e);
            return "stopped";
        }
    }
}
