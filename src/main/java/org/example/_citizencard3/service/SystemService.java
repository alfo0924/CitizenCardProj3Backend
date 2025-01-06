package org.example._citizencard3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizencard3.dto.response.DashboardStatsResponse;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.model.DiscountCoupon;
import org.example._citizencard3.model.MovieTicket;
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

            // 修改優惠券狀態查詢
            DashboardStatsResponse response = DashboardStatsResponse.builder()
                    .success(true)
                    .totalUsers(getCountSafely(() -> userRepository.count(), "總用戶數"))
                    .newUsers(getCountSafely(() -> userRepository.countByCreatedAtAfter(oneMonthAgo), "新用戶數"))
                    .activeUsers(getCountSafely(() -> userRepository.countByLastLoginTimeAfter(oneMonthAgo), "活躍用戶數"))
                    .totalStores(getCountSafely(() -> storeRepository.countByActiveTrue(), "總商店數"))
                    .newStores(getCountSafely(() -> storeRepository.countByActiveTrueAndCreatedAtAfter(oneMonthAgo), "新商店數"))
                    .activeMovies(getCountSafely(() -> movieRepository.countByIsShowingTrueAndActiveTrue(), "上映電影數"))
                    .newMovies(getCountSafely(() -> movieRepository.countByCreatedAtAfterAndActiveTrue(oneMonthAgo), "新電影數"))
                    .totalBalance(getDoubleSafely(() -> walletRepository.sumBalance(), "總餘額"))
                    .averageBalance(getDoubleSafely(() -> walletRepository.averageBalance(), "平均餘額"))
                    .totalTickets(getCountSafely(() -> movieTicketRepository.count(), "總票券數"))
                    .validTickets(getCountSafely(() -> movieTicketRepository.countByStatus(MovieTicket.TicketStatus.VALID), "有效票券數"))
                    .ticketsSoldToday(getCountSafely(() -> movieTicketRepository.countByCreatedAtAfter(startOfDay), "今日售票數"))
                    .totalCoupons(getCountSafely(() -> discountCouponRepository.count(), "總優惠券數"))
                    .activeCoupons(getCountSafely(() -> discountCouponRepository.countByStatus(DiscountCoupon.CouponStatus.VALID), "有效優惠券數"))
                    .couponsUsedToday(getCountSafely(() -> discountCouponRepository.countByStatusAndUpdatedAtAfter(DiscountCoupon.CouponStatus.USED, startOfDay), "今日使用優惠券數"))
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


    public Map<String, Long> getMovieGenreDistribution() {
        try {
            Map<String, Long> distribution = new HashMap<>();
            List<MovieRepository.GenreCount> results = movieRepository.countGroupByGenre();

            for (MovieRepository.GenreCount result : results) {
                String genre = result.getGenre();
                Long count = result.getCount();
                distribution.put(genre != null ? genre : "其他", count);
            }
            return distribution;
        } catch (Exception e) {
            log.error("獲取電影類型分佈失敗", e);
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

    public Map<String, Long> getUserRoleDistribution() {
        try {
            Map<String, Long> distribution = new HashMap<>();
            List<UserRepository.RoleCount> results = userRepository.countGroupByRole();

            for (UserRepository.RoleCount result : results) {
                String role = result.getRole();
                Long count = result.getCount();
                distribution.put(role, count);
            }
            return distribution;
        } catch (Exception e) {
            log.error("獲取使用者角色分佈失敗", e);
            return new HashMap<>();
        }
    }

    public Map<String, Long> getStoreCategoryDistribution() {
        try {
            Map<String, Long> distribution = new HashMap<>();
            List<Object[]> results = storeRepository.countGroupByCategory();

            for (Object[] result : results) {
                String category = (String) result[0];
                Long count = (Long) result[1];
                distribution.put(category, count);
            }

            return distribution;
        } catch (Exception e) {
            log.error("獲取商店類別分佈失敗", e);
            return new HashMap<>();
        }
    }




    private Long getValidCouponsCount() {
        return discountCouponRepository.countByStatus(DiscountCoupon.CouponStatus.VALID);
    }

    private Long getUsedCouponsCount() {
        return discountCouponRepository.countByStatus(DiscountCoupon.CouponStatus.USED);
    }


}
