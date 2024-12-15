package org.example._citizencard3.repository;

import org.example._citizencard3.model.MovieTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MovieTicketRepository extends JpaRepository<MovieTicket, Long> {

    Page<MovieTicket> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<MovieTicket> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status, Pageable pageable);

    boolean existsByUserIdAndMovieIdAndScheduleIdAndSeatNumber(
            Long userId,
            Long movieId,
            Long scheduleId,
            String seatNumber
    );

    Page<MovieTicket> findByMovieIdAndScheduleId(Long movieId, Long scheduleId, Pageable pageable);

    /**
     * 計算指定狀態的電影票數量
     * @param status 電影票狀態
     * @return 符合指定狀態的電影票數量
     */
    long countByStatusEquals(String status);

    /**
     * 計算在指定日期之後創建的電影票數量
     * @param dateTime 指定的日期時間
     * @return 在指定日期之後創建的電影票數量
     */
    long countByCreatedAtAfter(LocalDateTime dateTime);

}