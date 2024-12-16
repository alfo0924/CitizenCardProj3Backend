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
            LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0);

            DashboardStatsResponse.DashboardStatsResponseBuilder builder = DashboardStatsResponse.builder().success(true);

            // 基礎統計
            builder.totalUsers(getCountSafely(() -> userRepository.count(), "總用戶數"))
                    .newUsers(getCountSafely(() -> userRepository.countByCreatedAtAfter(oneMonthAgo), "新用戶數"))
                    .activeUsers(getCountSafely(() -> userRepository.countByLastLoginTimeAfter(oneMonthAgo), "活躍用戶數"))
                    .totalStores(getCountSafely(() -> storeRepository.countByActiveTrue(), "總商店數"))
                    .newStores(getCountSafely(() -> storeRepository.countByActiveTrueAndCreatedAtAfter(oneMonthAgo), "新商店數"))
                    .activeMovies(getCountSafely(() -> movieRepository.countByIsShowingTrueAndActiveTrue(), "上映電影數"))
                    .newMovies(getCountSafely(() -> movieRepository.countByCreatedAtAfterAndActiveTrue(oneMonthAgo), "新電影數"));

            // 財務統計
            builder.totalBalance(getDoubleSafely(() -> walletRepository.sumBalance(), "總餘額"))
                    .averageBalance(getDoubleSafely(() -> walletRepository.averageBalance(), "平均餘額"));

            // 票券統計
            builder.totalTickets(getCountSafely(() -> movieTicketRepository.count(), "總票券數"))
                    .validTickets(getCountSafely(() -> movieTicketRepository.countByStatusEquals("VALID"), "有效票券數"))
                    .ticketsSoldToday(getCountSafely(() -> movieTicketRepository.countByCreatedAtAfter(startOfDay), "今日售票數"));

            // 優惠券統計
            builder.totalCoupons(getCountSafely(() -> discountCouponRepository.count(), "總優惠券數"))
                    .activeCoupons(getCountSafely(() -> discountCouponRepository.countByStatusEquals("VALID"), "有效優惠券數"))
                    .couponsUsedToday(getCountSafely(() -> discountCouponRepository.countByStatusEqualsAndUpdatedAtAfter("USED", startOfDay), "今日使用優惠券數"));

            // 分佈統計
            builder.userRoleDistribution(getUserRoleDistribution())
                    .movieGenreDistribution(getMovieGenreDistribution())
                    .storeCategoryDistribution(getStoreCategoryDistribution());

            builder.timestamp(now);

            return builder.build();
        } catch (Exception e) {
            log.error("獲取儀表板數據失敗", e);
            throw new CustomException("獲取儀表板數據失敗: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private long getCountSafely(CountSupplier supplier, String errorMessage) {
        try {
            return supplier.getCount();
        } catch (Exception e) {
            log.error("獲取{}失敗", errorMessage, e);
            return 0;
        }
    }

    private double getDoubleSafely(DoubleSupplier supplier, String errorMessage) {
        try {
            return supplier.getDouble();
        } catch (Exception e) {
            log.error("獲取{}失敗", errorMessage, e);
            return 0.0;
        }
    }

    @Cacheable(value = "userRoleDistribution", unless = "#result.isEmpty()")
    public Map<String, Long> getUserRoleDistribution() {
        try {
            Map<String, Long> distribution = userRepository.countByRole();
            return distribution != null ? distribution : new HashMap<>();
        } catch (Exception e) {
            log.error("獲取用戶角色分佈失敗", e);
            return new HashMap<>();
        }
    }

    @Cacheable(value = "movieGenreDistribution", unless = "#result.isEmpty()")
    public Map<String, Long> getMovieGenreDistribution() {
        try {
            Map<String, Long> distribution = movieRepository.countByGenre();
            return distribution != null ? distribution : new HashMap<>();
        } catch (Exception e) {
            log.error("獲取電影類型分佈失敗", e);
            return new HashMap<>();
        }
    }

    @Cacheable(value = "storeCategoryDistribution", unless = "#result.isEmpty()")
    public Map<String, Long> getStoreCategoryDistribution() {
        try {
            Map<String, Long> distribution = storeRepository.countByCategory();
            return distribution != null ? distribution : new HashMap<>();
        } catch (Exception e) {
            log.error("獲取商店類別分佈失敗", e);
            return new HashMap<>();
        }
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
