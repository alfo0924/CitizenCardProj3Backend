package org.example._citizencard3.controller;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.dto.response.DashboardStatsResponse;
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
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        try {
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

            DashboardStatsResponse response = DashboardStatsResponse.builder()
                    .totalUsers(userService.countAllUsers())
                    .newUsers(userService.countNewUsersAfter(oneMonthAgo))
                    .activeUsers(userService.countByLastLoginTimeAfter(oneMonthAgo))
                    .totalStores(storeService.countActiveStores())
                    .newStores(storeService.countNewStoresAfter(oneMonthAgo))
                    .activeMovies(movieService.countActiveMovies())
                    .newMovies(movieService.countNewMoviesAfter(oneMonthAgo))
                    .totalBalance(walletService.sumBalance())
                    .averageBalance(walletService.averageBalance())
                    .userRoleDistribution(userService.getUserRoleDistribution())
                    .storeCategoryDistribution(storeService.getStoreCategoryDistribution())
                    .recentLogins(userService.getRecentLogins(10))
                    .recentTransactions(walletService.getRecentTransactions(10))
                    .recentMovieBookings(movieService.getRecentBookings(10))
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    DashboardStatsResponse.builder()
                            .success(false)
                            .message("獲取儀表板數據失敗")
                            .error(e.getMessage())
                            .build()
            );
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
            status.put("databaseStatus", checkDatabaseStatus());
            status.put("apiStatus", checkApiStatus());
            status.put("cacheStatus", checkCacheStatus());

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("success", false);
            errorStatus.put("message", "系統狀態檢查失敗");
            errorStatus.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorStatus);
        }
    }

    private String checkDatabaseStatus() {
        // 實現數據庫連接檢查邏輯
        return "connected";
    }

    private String checkApiStatus() {
        // 實現 API 服務檢查邏輯
        return "operational";
    }

    private String checkCacheStatus() {
        // 實現緩存服務檢查邏輯
        return "running";
    }
}
