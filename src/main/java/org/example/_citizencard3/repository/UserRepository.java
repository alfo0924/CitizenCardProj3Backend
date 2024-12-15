package org.example._citizencard3.repository;

import org.example._citizencard3.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailAndActive(String email, boolean active);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> findByNameContainingOrEmailContaining(
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.active = true")
    List<User> findActiveUsersByRole(@Param("role") String role, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true")
    long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    long countNewUsers(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT u FROM User u WHERE u.lastLoginTime IS NOT NULL " +
            "ORDER BY u.lastLoginTime DESC")
    List<User> findRecentlyActiveUsers(Pageable pageable);

    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> getUserCountByRole();

    @Query("SELECT u FROM User u WHERE u.lastLoginTime < :date AND u.active = true")
    List<User> findInactiveUsers(@Param("date") LocalDateTime date);

    @Modifying
    @Query("UPDATE User u SET u.active = :status WHERE u.id = :userId")
    int updateUserStatus(@Param("userId") Long userId, @Param("status") boolean status);

    @Query("SELECT COUNT(u) FROM User u WHERE " +
            "u.createdAt BETWEEN :startDate AND :endDate")
    long countUserRegistrationsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT u FROM User u WHERE " +
            "u.emailVerified = false AND " +
            "u.createdAt <= :cutoffDate")
    List<User> findUnverifiedUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.wallet " +
            "WHERE u.email = :email")
    Optional<User> findByEmailWithWallet(@Param("email") String email);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.movieTickets " +
            "LEFT JOIN FETCH u.discountCoupons " +
            "WHERE u.id = :userId")
    Optional<User> findByIdWithDetails(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginTime = :loginTime WHERE u.email = :email")
    void updateLastLoginTime(@Param("email") String email, @Param("loginTime") LocalDateTime loginTime);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.emailVerified = false")
    Optional<User> findUnverifiedUserByEmail(@Param("email") String email);

    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.email = :email")
    void verifyEmail(@Param("email") String email);
}
