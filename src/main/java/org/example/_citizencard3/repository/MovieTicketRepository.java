package org.example._citizencard3.repository;

import org.example._citizencard3.model.MovieTicket;
import org.example._citizencard3.model.MovieTicket.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovieTicketRepository extends JpaRepository<MovieTicket, Long> {

    // 基本查詢方法
    Page<MovieTicket> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 使用枚舉類型來查詢狀態
    Page<MovieTicket> findByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            TicketStatus status,
            Pageable pageable
    );

    // 檢查票券是否存在
    boolean existsByUserIdAndMovieIdAndScheduleIdAndSeatNumber(
            Long userId,
            Long movieId,
            Long scheduleId,
            String seatNumber
    );

    // 查詢特定電影和場次的票券
    Page<MovieTicket> findByMovieIdAndScheduleIdOrderByCreatedAtDesc(
            Long movieId,
            Long scheduleId,
            Pageable pageable
    );

    // 統計查詢
    long countByStatus(TicketStatus status);

    // 根據狀態字串查詢數量
    @Query("SELECT COUNT(t) FROM MovieTicket t WHERE t.status = :status")
    long countByStatusEquals(@Param("status") TicketStatus status);

    long countByCreatedAtAfter(LocalDateTime dateTime);

    // 自定義查詢即將到來的票券
    @Query("SELECT t FROM MovieTicket t WHERE t.userId = :userId AND " +
            "t.status = :status AND t.schedule.showTime > :now")
    Page<MovieTicket> findUpcomingTickets(
            @Param("userId") Long userId,
            @Param("status") TicketStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    // 查詢過去的票券
    @Query("SELECT t FROM MovieTicket t WHERE t.userId = :userId AND " +
            "t.status = :status AND t.schedule.showTime <= :now")
    Page<MovieTicket> findPastTickets(
            @Param("userId") Long userId,
            @Param("status") TicketStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    // 查詢用戶的所有有效票券
    @Query("SELECT t FROM MovieTicket t WHERE t.userId = :userId AND " +
            "t.status = 'VALID' AND t.schedule.showTime > :now")
    List<MovieTicket> findValidTicketsByUser(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

    // 查詢即將過期的票券
    @Query("SELECT t FROM MovieTicket t WHERE t.status = 'VALID' AND " +
            "t.schedule.showTime BETWEEN :start AND :end")
    List<MovieTicket> findTicketsAboutToExpire(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
