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

            // 基礎統計
            long totalUsers = userRepository.count();
            long newUsers = userRepository.countByCreatedAtAfter(oneMonthAgo);
            long activeUsers = userRepository.countByLastLoginTimeAfter(oneMonthAgo);

            long totalStores = storeRepository.countByActiveTrue();
            long newStores = storeRepository.countByActiveTrueAndCreatedAtAfter(oneMonthAgo);

            long activeMovies = movieRepository.countByIsShowingTrueAndActiveTrue();
            long newMovies = movieRepository.countByCreatedAtAfterAndActiveTrue(oneMonthAgo);

            // 財務統計
            double totalBalance = walletRepository.sumBalance() != null ? walletRepository.sumBalance() : 0.0;
            double averageBalance = walletRepository.averageBalance() != null ? walletRepository.averageBalance() : 0.0;

            // 票券統計
            long totalTickets = movieTicketRepository.count();
            long validTickets = movieTicketRepository.countByStatusEquals("VALID");
            long ticketsSoldToday = movieTicketRepository.countByCreatedAtAfter(startOfDay);

            // 優惠券統計
            long totalCoupons = discountCouponRepository.count();
            long activeCoupons = discountCouponRepository.countByStatusEquals("VALID");
            long couponsUsedToday = discountCouponRepository.countByStatusEqualsAndUpdatedAtAfter("USED", startOfDay);

            // 分佈統計
            Map<String, Long> userRoleDistribution = getUserRoleDistribution();
            Map<String, Long> movieGenreDistribution = getMovieGenreDistribution();
            Map<String, Long> storeCategoryDistribution = getStoreCategoryDistribution();

            return DashboardStatsResponse.builder()
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
                    .totalTickets(totalTickets)
                    .validTickets(validTickets)
                    .ticketsSoldToday(ticketsSoldToday)
                    .totalCoupons(totalCoupons)
                    .activeCoupons(activeCoupons)
                    .couponsUsedToday(couponsUsedToday)
                    .userRoleDistribution(userRoleDistribution)
                    .movieGenreDistribution(movieGenreDistribution)
                    .storeCategoryDistribution(storeCategoryDistribution)
                    .timestamp(now)
                    .build();

        } catch (Exception e) {
            log.error("獲取儀表板數據失敗", e);
            throw new CustomException("獲取儀表板數據失敗: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
}
