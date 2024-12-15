package org.example._citizencard3.mapper;

import org.example._citizencard3.dto.response.DashboardStatsResponse;
import org.example._citizencard3.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DashboardMapper {

    public DashboardStatsResponse toDashboardStatsResponse(
            long totalUsers,
            long newUsers,
            long activeMovies,
            long newMovies,
            long totalStores,
            long newStores,
            double totalBalance,
            long totalTickets,
            long validTickets,
            long totalCoupons,
            long activeCoupons,
            long lastLoginCount,
            long ticketsSoldToday,
            long couponsUsedToday,
            List<User> users,
            List<Movie> movies,
            List<Store> stores
    ) {
        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .newUsers(newUsers)
                .activeMovies(activeMovies)
                .newMovies(newMovies)
                .totalStores(totalStores)
                .newStores(newStores)
                .totalBalance(totalBalance)
                .averageBalance(calculateAverageBalance(totalBalance, totalUsers))
                .totalTickets(totalTickets)
                .validTickets(validTickets)
                .totalCoupons(totalCoupons)
                .activeCoupons(activeCoupons)
                .lastLoginCount(lastLoginCount)
                .ticketsSoldToday(ticketsSoldToday)
                .couponsUsedToday(couponsUsedToday)
                .userRoleDistribution(getUserRoleDistribution(users))
                .movieGenreDistribution(getMovieGenreDistribution(movies))
                .storeCategoryDistribution(getStoreCategoryDistribution(stores))
                .build();
    }

    private double calculateAverageBalance(double totalBalance, long totalUsers) {
        return totalUsers > 0 ? totalBalance / totalUsers : 0;
    }

    private Map<String, Long> getUserRoleDistribution(List<User> users) {
        return users.stream()
                .collect(Collectors.groupingBy(User::getRole, Collectors.counting()));
    }

    private Map<String, Long> getMovieGenreDistribution(List<Movie> movies) {
        return movies.stream()
                .collect(Collectors.groupingBy(Movie::getGenre, Collectors.counting()));
    }

    private Map<String, Long> getStoreCategoryDistribution(List<Store> stores) {
        return stores.stream()
                .collect(Collectors.groupingBy(Store::getCategory, Collectors.counting()));
    }
}
