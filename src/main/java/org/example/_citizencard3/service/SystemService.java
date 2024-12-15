package org.example._citizencard3.service;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.dto.response.DashboardStatsResponse;
import org.example._citizencard3.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemService {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final StoreRepository storeRepository;
    private final WalletRepository walletRepository;
    private final MovieTicketRepository movieTicketRepository;
    private final DiscountCouponRepository discountCouponRepository;

    public DashboardStatsResponse getDashboardStats() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        long totalUsers = userRepository.count();
        long newUsers = userRepository.countByCreatedAtAfter(oneMonthAgo);

        long activeMovies = movieRepository.countByIsShowingTrueAndActiveTrue();
        long newMovies = movieRepository.countByCreatedAtAfterAndActiveTrue(oneMonthAgo);

        long totalStores = storeRepository.countByActiveTrue();
        long newStores = storeRepository.countByActiveTrueAndCreatedAtAfter(oneMonthAgo);

        double totalBalance = walletRepository.sumBalance();
        double averageBalance = walletRepository.averageBalance();

        long totalTickets = movieTicketRepository.count();
        long validTickets = movieTicketRepository.countByStatusEquals("VALID");

        long totalCoupons = discountCouponRepository.count();
        long activeCoupons = discountCouponRepository.countByStatusEquals("VALID");

        long lastLoginCount = userRepository.countByLastLoginTimeAfter(oneMonthAgo);
        long ticketsSoldToday = movieTicketRepository.countByCreatedAtAfter(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
        long couponsUsedToday = discountCouponRepository.countByStatusEqualsAndUpdatedAtAfter("USED", LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));

        Map<String, Long> userRoleDistribution = getUserRoleDistribution();
        Map<String, Long> movieGenreDistribution = getMovieGenreDistribution();
        Map<String, Long> storeCategoryDistribution = getStoreCategoryDistribution();

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .newUsers(newUsers)
                .activeMovies(activeMovies)
                .newMovies(newMovies)
                .totalStores(totalStores)
                .newStores(newStores)
                .totalBalance(totalBalance)
                .averageBalance(averageBalance)
                .totalTickets(totalTickets)
                .validTickets(validTickets)
                .totalCoupons(totalCoupons)
                .activeCoupons(activeCoupons)
                .lastLoginCount(lastLoginCount)
                .ticketsSoldToday(ticketsSoldToday)
                .couponsUsedToday(couponsUsedToday)
                .userRoleDistribution(userRoleDistribution)
                .movieGenreDistribution(movieGenreDistribution)
                .storeCategoryDistribution(storeCategoryDistribution)
                .build();
    }

    private Map<String, Long> getUserRoleDistribution() {
        return userRepository.countByRole();
    }

    private Map<String, Long> getMovieGenreDistribution() {
        return movieRepository.countByGenre();
    }

    private Map<String, Long> getStoreCategoryDistribution() {
        return storeRepository.countByCategory();
    }
}
