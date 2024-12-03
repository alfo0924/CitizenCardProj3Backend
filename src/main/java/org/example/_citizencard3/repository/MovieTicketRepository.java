package org.example._citizencard3.repository;

import org.example._citizencard3.model.MovieTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovieTicketRepository extends JpaRepository<MovieTicket, Long> {

    // 根據用戶ID查詢電影票
    List<MovieTicket> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 根據狀態查詢電影票
    List<MovieTicket> findByStatus(MovieTicket.TicketStatus status);

    // 根據用戶ID和狀態查詢電影票
    List<MovieTicket> findByUserIdAndStatus(Long userId, MovieTicket.TicketStatus status);

    // 根據電影ID查詢電影票
    List<MovieTicket> findByMovieId(Long movieId);

    // 根據場次ID查詢電影票
    List<MovieTicket> findByScheduleId(Long scheduleId);

    // 檢查座位是否已被預訂
    boolean existsByScheduleIdAndSeatNumber(Long scheduleId, String seatNumber);

    // 統計某場次的售出票數
    @Query("SELECT COUNT(t) FROM MovieTicket t WHERE t.schedule.id = :scheduleId AND t.status != 'CANCELLED'")
    long countTicketsByScheduleId(@Param("scheduleId") Long scheduleId);

    // 查詢指定時間範圍內的有效票券
    @Query("SELECT t FROM MovieTicket t WHERE t.schedule.showTime BETWEEN :startTime AND :endTime AND t.status = 'VALID'")
    List<MovieTicket> findValidTicketsBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 查詢用戶最近的電影票
    @Query("SELECT t FROM MovieTicket t WHERE t.user.id = :userId AND t.status = 'VALID' ORDER BY t.schedule.showTime DESC")
    List<MovieTicket> findRecentValidTickets(@Param("userId") Long userId);

    // 查詢即將過期的電影票
    @Query("SELECT t FROM MovieTicket t WHERE t.status = 'VALID' AND t.schedule.showTime < :expiryTime")
    List<MovieTicket> findExpiringSoonTickets(@Param("expiryTime") LocalDateTime expiryTime);
}