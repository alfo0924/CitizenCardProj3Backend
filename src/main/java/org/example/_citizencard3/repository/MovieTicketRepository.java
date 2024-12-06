package org.example._citizencard3.repository;

import org.example._citizencard3.model.MovieTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}