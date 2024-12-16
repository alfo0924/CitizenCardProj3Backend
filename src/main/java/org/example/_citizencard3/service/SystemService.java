package org.example._citizencard3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizencard3.dto.response.DashboardStatsResponse;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.repository.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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

    @Cacheable(value = "dashboardStats", key = "'stats'", unless = "#result == null")
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneMonthAgo = now.minusMonths(1);
            LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0);

            // 用戶統計
            Map<String, Long> userStats = getUserStats(oneMonthAgo);

            // 電影統計
            Map<String, Long> movieStats = getMovieStats(oneMonthAgo);

            // 商店統計
            Map<String, Long> storeStats = getStoreStats(oneMonthAgo);

            // 交易統計
            Map<String, Double> transactionStats = getTransactionStats();

            // 票券統計
            Map<String, Long> ticketStats = getTicketStats(startOfDay);

            // 優惠券統計
            Map<String, Long> couponStats = getCouponStats(startOfDay);

            // 分佈統計
            Map<String, Long> userRoleDistribution = getUserRoleDistribution();
            Map<String, Long> movieGenreDistribution = getMovieGenreDistribution();
            Map<String, Long> storeCategoryDistribution = getStoreCategoryDistribution();

            return buildDashboardResponse(
                    userStats,
                    movieStats,
                    storeStats,
                    transactionStats,
                    ticketStats,
                    couponStats,
                    userRoleDistribution,
                    movieGenreDistribution,
                    storeCategoryDistribution
            );
        } catch (Exception e) {
            log.error("獲取儀表板數據失敗", e);
            throw new CustomException("獲取儀表板數據失敗: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, Long> getUserStats(LocalDateTime oneMonthAgo) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("newUsers", userRepository.countByCreatedAtAfter(oneMonthAgo));
        stats.put("activeUsers", userRepository.countByLastLoginTimeAfter(oneMonthAgo));
        return stats;
    }

    private Map<String, Long> getMovieStats(LocalDateTime oneMonthAgo) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("activeMovies", movieRepository.countByIsShowingTrueAndActiveTrue());
        stats.put("newMovies", movieRepository.countByCreatedAtAfterAndActiveTrue(oneMonthAgo));
        return stats;
    }

    private Map<String, Long> getStoreStats(LocalDateTime oneMonthAgo) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalStores", storeRepository.countByActiveTrue());
        stats.put("newStores", storeRepository.countByActiveTrueAndCreatedAtAfter(oneMonthAgo));
        return stats;
    }

    private Map<String, Double> getTransactionStats() {
        Map<String, Double> stats = new HashMap<>();
        stats.put("totalBalance", walletRepository.sumBalance());
        stats.put("averageBalance", walletRepository.averageBalance());
        return stats;
    }

    private Map<String, Long> getTicketStats(LocalDateTime startOfDay) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalTickets", movieTicketRepository.count());
        stats.put("validTickets", movieTicketRepository.countByStatusEquals("VALID"));
        stats.put("ticketsSoldToday", movieTicketRepository.countByCreatedAtAfter(startOfDay));
        return stats;
    }

    private Map<String, Long> getCouponStats(LocalDateTime startOfDay) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalCoupons", discountCouponRepository.count());
        stats.put("activeCoupons", discountCouponRepository.countByStatusEquals("VALID"));
        stats.put("couponsUsedToday", discountCouponRepository.countByStatusEqualsAndUpdatedAtAfter("USED", startOfDay));
        return stats;
    }

    @Cacheable(value = "userRoleDistribution", unless = "#result.isEmpty()")
    public Map<String, Long> getUserRoleDistribution() {
        return userRepository.countByRole();
    }

    @Cacheable(value = "movieGenreDistribution", unless = "#result.isEmpty()")
    public Map<String, Long> getMovieGenreDistribution() {
        return movieRepository.countByGenre();
    }

    @Cacheable(value = "storeCategoryDistribution", unless = "#result.isEmpty()")
    public Map<String, Long> getStoreCategoryDistribution() {
        return storeRepository.countByCategory();
    }


    private DashboardStatsResponse buildDashboardResponse(
            Map<String, Long> userStats,
            Map<String, Long> movieStats,
            Map<String, Long> storeStats,
            Map<String, Double> transactionStats,
            Map<String, Long> ticketStats,
            Map<String, Long> couponStats,
            Map<String, Long> userRoleDistribution,
            Map<String, Long> movieGenreDistribution,
            Map<String, Long> storeCategoryDistribution
    ) {
        return DashboardStatsResponse.builder()
                .totalUsers(userStats.get("totalUsers"))
                .newUsers(userStats.get("newUsers"))
                .activeUsers(userStats.get("activeUsers"))
                .activeMovies(movieStats.get("activeMovies"))
                .newMovies(movieStats.get("newMovies"))
                .totalStores(storeStats.get("totalStores"))
                .newStores(storeStats.get("newStores"))
                .totalBalance(transactionStats.get("totalBalance"))
                .averageBalance(transactionStats.get("averageBalance"))
                .totalTickets(ticketStats.get("totalTickets"))
                .validTickets(ticketStats.get("validTickets"))
                .ticketsSoldToday(ticketStats.get("ticketsSoldToday"))
                .totalCoupons(couponStats.get("totalCoupons"))
                .activeCoupons(couponStats.get("activeCoupons"))
                .couponsUsedToday(couponStats.get("couponsUsedToday"))
                .userRoleDistribution(userRoleDistribution)
                .movieGenreDistribution(movieGenreDistribution)
                .storeCategoryDistribution(storeCategoryDistribution)
                .build();
    }
}
