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

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        log.info("Receiving dashboard statistics request");
        try {
            DashboardStatsResponse response = systemService.getDashboardStats();

            if (!response.isSuccess()) {
                log.warn("Failed to fetch dashboard stats: {}", response.getError());
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(response);
            }

            log.info("Successfully fetched dashboard statistics");
            return ResponseEntity.ok(response);

        } catch (CustomException e) {
            log.error("Custom error while fetching dashboard statistics", e);
            return ResponseEntity
                    .status(e.getStatus())
                    .body(DashboardStatsResponse.builder()
                            .success(false)
                            .message("獲取儀表板數據失敗")
                            .error(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());

        } catch (Exception e) {
            log.error("Unexpected error while fetching dashboard statistics", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DashboardStatsResponse.builder()
                            .success(false)
                            .message("系統錯誤")
                            .error("獲取儀表板數據時發生未預期的錯誤")
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> checkSystemStatus() {
        log.info("Checking system status");
        try {
            Map<String, String> statusChecks = systemService.checkSystemStatus();

            boolean allSystemsOperational = statusChecks.values()
                    .stream()
                    .allMatch(status -> "operational".equals(status) ||
                            "connected".equals(status) ||
                            "running".equals(status));

            Map<String, Object> response = new HashMap<>();
            response.put("success", allSystemsOperational);
            response.put("timestamp", LocalDateTime.now());
            response.put("statuses", statusChecks);

            if (!allSystemsOperational) {
                response.put("message", "部分系統服務異常");
                return ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(response);
            }

            log.info("System status check completed successfully");
            return ResponseEntity.ok(response);

        } catch (CustomException e) {
            log.error("Custom error during system status check", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "系統狀態檢查失敗");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity
                    .status(e.getStatus())
                    .body(errorResponse);

        } catch (Exception e) {
            log.error("Unexpected error during system status check", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "系統錯誤");
            errorResponse.put("error", "系統狀態檢查時發生未預期的錯誤");
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
}
