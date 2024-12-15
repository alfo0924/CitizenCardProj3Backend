package org.example._citizencard3.repository;

import org.example._citizencard3.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByMovie_Id(Long movieId);

    List<Schedule> findByMovie_IdAndActive(Long movieId, Boolean active);

    @Query("SELECT s FROM Schedule s WHERE s.movie.id = :movieId AND s.showTime >= :startTime AND s.showTime <= :endTime AND s.active = true")
    List<Schedule> findByMovieIdAndDateRange(
            @Param("movieId") Long movieId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT s FROM Schedule s WHERE s.showTime >= :startTime AND s.showTime <= :endTime AND s.active = true")
    List<Schedule> findByDateRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    List<Schedule> findByHallAndShowTimeBetween(
            String hall,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    @Query("SELECT s FROM Schedule s WHERE s.availableSeats > 0 AND s.showTime >= :now AND s.active = true")
    List<Schedule> findAvailableSchedules(@Param("now") LocalDateTime now);

    @Query("SELECT s FROM Schedule s WHERE s.movie.id = :movieId AND s.availableSeats > 0 AND s.showTime >= :now AND s.active = true")
    List<Schedule> findAvailableSchedulesByMovie(
            @Param("movieId") Long movieId,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT COUNT(s) FROM Schedule s WHERE s.movie.id = :movieId AND s.showTime >= :startTime AND s.showTime <= :endTime")
    Long countSchedulesByMovieAndDateRange(
            @Param("movieId") Long movieId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    List<Schedule> findByShowTimeBefore(LocalDateTime now);

    @Query("SELECT s FROM Schedule s WHERE s.movie.id = :movieId AND s.showTime >= :now ORDER BY s.showTime ASC")
    List<Schedule> findUpcomingSchedulesByMovie(
            @Param("movieId") Long movieId,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT s FROM Schedule s WHERE s.hall = :hall AND s.showTime >= :startTime AND s.showTime <= :endTime AND s.active = true")
    List<Schedule> findActiveSchedulesByHallAndDateRange(
            @Param("hall") String hall,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    List<Schedule> findByMovieIdAndActive(Long movieId, boolean b);
}
