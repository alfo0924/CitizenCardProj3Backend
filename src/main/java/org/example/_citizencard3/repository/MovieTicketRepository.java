package org.example._citizencard3.repository;

import org.example._citizencard3.model.Movie;
import org.example._citizencard3.model.MovieTicket;
import org.example._citizencard3.model.MovieTicket.TicketStatus;
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
public interface MovieTicketRepository extends JpaRepository<MovieTicket, Long> {

    // 基本查詢方法
    Page<MovieTicket> findByUserId(Long userId, Pageable pageable);

    Page<MovieTicket> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT t FROM MovieTicket t WHERE t.userId = :userId ORDER BY t.schedule.showTime DESC")
    Page<MovieTicket> findByUserIdOrderByShowTimeDesc(Long userId, Pageable pageable);

    // 依狀態查詢票券
    Page<MovieTicket> findByUserIdAndStatus(Long userId, TicketStatus status, Pageable pageable);

    Page<MovieTicket> findByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            TicketStatus status,
            Pageable pageable
    );

    @Query("SELECT t FROM MovieTicket t WHERE t.userId = :userId AND t.status = :status " +
            "ORDER BY t.schedule.showTime DESC")
    Page<MovieTicket> findByUserIdAndStatusOrderByShowTimeDesc(
            Long userId,
            TicketStatus status,
            Pageable pageable
    );

    // 驗證票券
    Optional<MovieTicket> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserIdAndStatus(Long id, Long userId, TicketStatus status);

    boolean existsByUserIdAndMovieAndScheduleIdAndSeatNumber(
            Long userId, Movie movie, Long schedule_id, String seatNumber
    );

    // 查詢特定電影和場次的票券
    @Query("SELECT t FROM MovieTicket t WHERE t.movie.id = :movieId AND t.schedule.id = :scheduleId")
    Page<MovieTicket> findByMovieIdAndScheduleId(
            @Param("movieId") Long movieId,
            @Param("scheduleId") Long scheduleId,
            Pageable pageable
    );

    @Query("SELECT t FROM MovieTicket t WHERE t.movie.id = :movieId AND t.schedule.id = :scheduleId " +
            "ORDER BY t.createdAt DESC")
    Page<MovieTicket> findByMovieIdAndScheduleIdOrderByCreatedAtDesc(
            @Param("movieId") Long movieId,
            @Param("scheduleId") Long scheduleId,
            Pageable pageable
    );

    // 統計查詢
    long countByStatus(TicketStatus status);

    long countByUserIdAndStatus(Long userId, TicketStatus status);

    long countByCreatedAtAfter(LocalDateTime dateTime);

    @Query("SELECT COUNT(t) FROM MovieTicket t WHERE t.status = :status")
    long countByStatusEquals(@Param("status") TicketStatus status);

    @Query("SELECT COUNT(t) FROM MovieTicket t WHERE t.userId = :userId AND t.status = :status")
    long countByUserIdAndStatusEquals(
            @Param("userId") Long userId,
            @Param("status") TicketStatus status
    );

    // 查詢即將到來的票券
    @Query("SELECT t FROM MovieTicket t WHERE t.userId = :userId AND " +
            "t.status = :status AND t.schedule.showTime > :now " +
            "ORDER BY t.schedule.showTime ASC")
    Page<MovieTicket> findUpcomingTickets(
            @Param("userId") Long userId,
            @Param("status") TicketStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    // 查詢過去的票券
    @Query("SELECT t FROM MovieTicket t WHERE t.userId = :userId AND " +
            "t.status = :status AND t.schedule.showTime <= :now " +
            "ORDER BY t.schedule.showTime DESC")
    Page<MovieTicket> findPastTickets(
            @Param("userId") Long userId,
            @Param("status") TicketStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    // 查詢用戶的所有有效票券
    @Query("SELECT t FROM MovieTicket t WHERE t.userId = :userId AND " +
            "t.status = 'VALID' AND t.schedule.showTime > :now " +
            "ORDER BY t.schedule.showTime ASC")
    List<MovieTicket> findValidTicketsByUser(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

    // 查詢即將過期的票券
    @Query("SELECT t FROM MovieTicket t WHERE t.status = 'VALID' AND " +
            "t.schedule.showTime BETWEEN :start AND :end " +
            "ORDER BY t.schedule.showTime ASC")
    List<MovieTicket> findTicketsAboutToExpire(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 查詢特定時間範圍內的票券
    @Query("SELECT t FROM MovieTicket t WHERE t.userId = :userId AND " +
            "t.schedule.showTime BETWEEN :start AND :end " +
            "ORDER BY t.schedule.showTime DESC")
    Page<MovieTicket> findByUserIdAndShowTimeBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    // 查詢特定電影的所有票券
    @Query("SELECT t FROM MovieTicket t WHERE t.userId = :userId AND " +
            "t.movie.id = :movieId " +
            "ORDER BY t.schedule.showTime DESC")
    Page<MovieTicket> findByUserIdAndMovieId(
            @Param("userId") Long userId,
            @Param("movieId") Long movieId,
            Pageable pageable
    );

    // 自定義分頁查詢
    @Query("SELECT t FROM MovieTicket t WHERE " +
            "(:userId IS NULL OR t.userId = :userId) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:movieId IS NULL OR t.movie.id = :movieId) AND " +
            "(:startTime IS NULL OR t.schedule.showTime >= :startTime) AND " +
            "(:endTime IS NULL OR t.schedule.showTime <= :endTime) " +
            "ORDER BY t.schedule.showTime DESC")
    Page<MovieTicket> findTicketsWithFilters(
            @Param("userId") Long userId,
            @Param("status") TicketStatus status,
            @Param("movieId") Long movieId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable
    );

    // 批量更新過期票券狀態
    @Modifying
    @Query("UPDATE MovieTicket t SET t.status = 'EXPIRED', t.updatedAt = :now " +
            "WHERE t.status = 'VALID' AND t.schedule.showTime < :now")
    void updateExpiredTickets(@Param("now") LocalDateTime now);

    // 查詢用戶最近的票券購買記錄
    @Query("SELECT t FROM MovieTicket t " +
            "WHERE (:userId IS NULL OR t.userId = :userId) " +
            "ORDER BY t.createdAt DESC")
    List<MovieTicket> findRecentTicketsByUser(
            @Param("userId") Long userId,
            Pageable pageable
    );

    // 檢查座位是否已被預訂
    @Query("SELECT COUNT(t) > 0 FROM MovieTicket t " +
            "WHERE t.schedule.id = :scheduleId AND " +
            "t.seatNumber = :seatNumber AND " +
            "t.status = 'VALID'")
    boolean isSeatOccupied(
            @Param("scheduleId") Long scheduleId,
            @Param("seatNumber") String seatNumber
    );

    // 查詢場次的已售出座位
    @Query("SELECT t.seatNumber FROM MovieTicket t " +
            "WHERE t.schedule.id = :scheduleId AND t.status = 'VALID'")
    List<String> findOccupiedSeats(@Param("scheduleId") Long scheduleId);
}