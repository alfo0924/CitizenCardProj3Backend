package org.example._citizencard3.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizencard3.dto.response.DashboardStatsResponse;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.service.SystemService;
import org.springframework.http.HttpStatus;
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

    /**
     * 获取仪表板统计数据
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        log.info("Receiving dashboard statistics request");
        try {
            DashboardStatsResponse response = systemService.getDashboardStats();
            validateDashboardResponse(response);

            log.info("Successfully fetched dashboard statistics");
            return ResponseEntity.ok(response);

        } catch (CustomException e) {
            log.error("Custom error while fetching dashboard statistics: {}", e.getMessage(), e);
            return handleDashboardError(e, e.getStatus());
        } catch (Exception e) {
            log.error("Unexpected error while fetching dashboard statistics: {}", e.getMessage(), e);
            return handleDashboardError(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 检查系统状态
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> checkSystemStatus() {
        log.info("Checking system status");
        try {
            Map<String, String> statusChecks = systemService.checkSystemStatus();
            boolean allSystemsOperational = checkSystemsOperational(statusChecks);

            Map<String, Object> response = createStatusResponse(statusChecks, allSystemsOperational);

            HttpStatus status = allSystemsOperational ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
            log.info("System status check completed. All systems operational: {}", allSystemsOperational);

            return ResponseEntity.status(status).body(response);

        } catch (CustomException e) {
            log.error("Custom error during system status check: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(e.getStatus())
                    .body(createErrorResponse("系統狀態檢查失敗", e.getMessage()));

        } catch (Exception e) {
            log.error("Unexpected error during system status check: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("系統錯誤", "系統狀態檢查時發生未預期的錯誤"));
        }
    }

    /**
     * 清除系统缓存
     */
    @PostMapping("/cache/clear")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> clearCache() {
        log.info("Clearing system cache");
        try {
            systemService.clearCache();
            log.info("Cache cleared successfully");
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "緩存清除成功");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error clearing cache: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("緩存清除失敗", e.getMessage()));
        }
    }

    /**
     * 获取分布统计数据
     */
    @GetMapping("/distributions")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getDistributions() {
        log.info("Fetching distribution statistics");
        try {
            Map<String, Object> distributions = new HashMap<>();
            distributions.put("userRoles", systemService.getUserRoleDistribution());
            distributions.put("movieGenres", systemService.getMovieGenreDistribution());
//            distributions.put("storeCategories", systemService.getStoreCategoryDistribution());
            distributions.put("timestamp", LocalDateTime.now());
            distributions.put("success", true);

            return ResponseEntity.ok(distributions);
        } catch (Exception e) {
            log.error("Error fetching distributions: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("獲取分布統計失敗", e.getMessage()));
        }
    }

    // Private helper methods

    private void validateDashboardResponse(DashboardStatsResponse response) {
        if (!response.isSuccess()) {
            log.warn("Failed to fetch dashboard stats: {}", response.getError());
            throw new CustomException(response.getError(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean checkSystemsOperational(Map<String, String> statusChecks) {
        return statusChecks.values()
                .stream()
                .allMatch(status -> "operational".equals(status) ||
                        "connected".equals(status) ||
                        "running".equals(status));
    }

    private Map<String, Object> createStatusResponse(Map<String, String> statusChecks, boolean allSystemsOperational) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", allSystemsOperational);
        response.put("timestamp", LocalDateTime.now());
        response.put("statuses", statusChecks);
        if (!allSystemsOperational) {
            response.put("message", "部分系統服務異常");
        }
        return response;
    }

    private Map<String, Object> createErrorResponse(String message, String error) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("error", error);
        errorResponse.put("timestamp", LocalDateTime.now());
        return errorResponse;
    }

    private ResponseEntity<DashboardStatsResponse> handleDashboardError(Exception e, HttpStatus status) {
        return ResponseEntity
                .status(status)
                .body(DashboardStatsResponse.builder()
                        .success(false)
                        .message(status == HttpStatus.INTERNAL_SERVER_ERROR ? "系統錯誤" : "獲取儀表板數據失敗")
                        .error(e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}