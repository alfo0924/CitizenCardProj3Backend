package org.example._citizencard3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizencard3.dto.response.DashboardStatsResponse;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.repository.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemService {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final StoreRepository storeRepository;
    private final WalletRepository walletRepository;
    private final MovieTicketRepository movieTicketRepository;
    private final DiscountCouponRepository discountCouponRepository;

    private static final String DASHBOARD_CACHE = "dashboardStats";
    private static final String DISTRIBUTION_CACHE = "distributionStats";
    private static final int CACHE_TTL = 300; // 5 minutes

    @Cacheable(value = DASHBOARD_CACHE, key = "'stats'", unless = "#result == null")
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        log.info("Fetching dashboard statistics");
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneMonthAgo = now.minusMonths(1);
            LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0);

            // Build response with parallel data fetching
            CompletableFuture<Long> totalUsersFuture = CompletableFuture.supplyAsync(() ->
                    getCountSafely(() -> userRepository.count(), "總用戶數"));
            CompletableFuture<Long> newUsersFuture = CompletableFuture.supplyAsync(() ->
                    getCountSafely(() -> userRepository.countByCreatedAtAfter(oneMonthAgo), "新用戶數"));
            CompletableFuture<Long> activeUsersFuture = CompletableFuture.supplyAsync(() ->
                    getCountSafely(() -> userRepository.countByLastLoginTimeAfter(oneMonthAgo), "活躍用戶數"));

            CompletableFuture<Long> totalStoresFuture = CompletableFuture.supplyAsync(() ->
                    getCountSafely(() -> storeRepository.countByActiveTrue(), "總商店數"));
            CompletableFuture<Long> newStoresFuture = CompletableFuture.supplyAsync(() ->
                    getCountSafely(() -> storeRepository.countByActiveTrueAndCreatedAtAfter(oneMonthAgo), "新商店數"));

            CompletableFuture<Long> activeMoviesFuture = CompletableFuture.supplyAsync(() ->
                    getCountSafely(() -> movieRepository.countByIsShowingTrueAndActiveTrue(), "上映電影數"));
            CompletableFuture<Long> newMoviesFuture = CompletableFuture.supplyAsync(() ->
                    getCountSafely(() -> movieRepository.countByCreatedAtAfterAndActiveTrue(oneMonthAgo), "新電影數"));

            // Wait for all futures to complete
            CompletableFuture.allOf(
                    totalUsersFuture, newUsersFuture, activeUsersFuture,
                    totalStoresFuture, newStoresFuture,
                    activeMoviesFuture, newMoviesFuture
            ).join();

            // Build response
            DashboardStatsResponse response = DashboardStatsResponse.builder()
                    .success(true)
                    .totalUsers(totalUsersFuture.get())
                    .newUsers(newUsersFuture.get())
                    .activeUsers(activeUsersFuture.get())
                    .totalStores(totalStoresFuture.get())
                    .newStores(newStoresFuture.get())
                    .activeMovies(activeMoviesFuture.get())
                    .newMovies(newMoviesFuture.get())
                    .totalBalance(getDoubleSafely(() -> walletRepository.sumBalance(), "總餘額"))
                    .averageBalance(getDoubleSafely(() -> walletRepository.averageBalance(), "平均餘額"))
                    .totalTickets(getCountSafely(() -> movieTicketRepository.count(), "總票券數"))
                    .validTickets(getCountSafely(() -> movieTicketRepository.countByStatusEquals("VALID"), "有效票券數"))
                    .ticketsSoldToday(getCountSafely(() -> movieTicketRepository.countByCreatedAtAfter(startOfDay), "今日售票數"))
                    .totalCoupons(getCountSafely(() -> discountCouponRepository.count(), "總優惠券數"))
                    .activeCoupons(getCountSafely(() -> discountCouponRepository.countByStatusEquals("VALID"), "有效優惠券數"))
                    .couponsUsedToday(getCountSafely(() ->
                            discountCouponRepository.countByStatusEqualsAndUpdatedAtAfter("USED", startOfDay), "今日使用優惠券數"))
                    .userRoleDistribution(getUserRoleDistribution())
                    .movieGenreDistribution(getMovieGenreDistribution())
                    .storeCategoryDistribution(getStoreCategoryDistribution())
                    .timestamp(now)
                    .build();

            validateResponse(response);
            log.info("Successfully fetched dashboard statistics");
            return response;

        } catch (Exception e) {
            log.error("Failed to fetch dashboard statistics", e);
            throw new CustomException("獲取儀表板數據失敗: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateResponse(DashboardStatsResponse response) {
        if (response == null) {
            throw new CustomException("無效的響應數據", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (response.getUserRoleDistribution() == null ||
                response.getMovieGenreDistribution() == null ||
                response.getStoreCategoryDistribution() == null) {
            throw new CustomException("分佈數據無效", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Cacheable(value = DISTRIBUTION_CACHE, key = "'userRole'", unless = "#result.isEmpty()")
    public Map<String, Long> getUserRoleDistribution() {
        try {
            Map<String, Long> distribution = userRepository.countByRole();
            return distribution != null ? distribution : new HashMap<>();
        } catch (Exception e) {
            log.error("Failed to get user role distribution", e);
            return new HashMap<>();
        }
    }

    @Cacheable(value = DISTRIBUTION_CACHE, key = "'movieGenre'", unless = "#result.isEmpty()")
    public Map<String, Long> getMovieGenreDistribution() {
        try {
            Map<String, Long> distribution = movieRepository.countByGenre();
            return distribution != null ? distribution : new HashMap<>();
        } catch (Exception e) {
            log.error("Failed to get movie genre distribution", e);
            return new HashMap<>();
        }
    }

    @Cacheable(value = DISTRIBUTION_CACHE, key = "'storeCategory'", unless = "#result.isEmpty()")
    public Map<String, Long> getStoreCategoryDistribution() {
        try {
            Map<String, Long> distribution = storeRepository.countByCategory();
            return distribution != null ? distribution : new HashMap<>();
        } catch (Exception e) {
            log.error("Failed to get store category distribution", e);
            return new HashMap<>();
        }
    }

    private long getCountSafely(CountSupplier supplier, String metricName) {
        try {
            long count = supplier.getCount();
            log.debug("Successfully fetched count for {}: {}", metricName, count);
            return count;
        } catch (Exception e) {
            log.error("Failed to get count for {}", metricName, e);
            return 0L;
        }
    }

    private double getDoubleSafely(DoubleSupplier supplier, String metricName) {
        try {
            double value = supplier.getDouble();
            log.debug("Successfully fetched value for {}: {}", metricName, value);
            return value;
        } catch (Exception e) {
            log.error("Failed to get value for {}", metricName, e);
            return 0.0;
        }
    }

    @CacheEvict(value = {DASHBOARD_CACHE, DISTRIBUTION_CACHE}, allEntries = true)
    public void clearCache() {
        log.info("Cleared all dashboard caches");
    }

    public Map<String, String> checkSystemStatus() {
        Map<String, String> statusMap = new HashMap<>();

        // 檢查數據庫連接
        try {
            userRepository.count();
            statusMap.put("database", "connected");
        } catch (Exception e) {
            log.error("Database connection check failed", e);
            statusMap.put("database", "disconnected");
        }

        // 檢查緩存服務
        try {
            this.clearCache();
            statusMap.put("cache", "operational");
        } catch (Exception e) {
            log.error("Cache service check failed", e);
            statusMap.put("cache", "error");
        }

        // 檢查外部 API 服務（如果有的話）
        // 這裡假設有一個外部 API 服務，實際情況可能需要調整
        try {
            // 模擬外部 API 調用
            // externalApiService.healthCheck();
            statusMap.put("externalApi", "operational");
        } catch (Exception e) {
            log.error("External API service check failed", e);
            statusMap.put("externalApi", "error");
        }

        // 檢查應用服務器狀態
        statusMap.put("appServer", "running");

        return statusMap;
    }


    @FunctionalInterface
    private interface CountSupplier {
        long getCount() throws Exception;
    }

    @FunctionalInterface
    private interface DoubleSupplier {
        double getDouble() throws Exception;
    }
}
