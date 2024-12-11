package org.example._citizencard3.service;

import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.model.Movie;
import org.example._citizencard3.model.Schedule;
import org.example._citizencard3.repository.MovieRepository;
import org.example._citizencard3.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MovieRepository movieRepository;

    // 基本查詢方法
    public List<Schedule> findAllSchedules() {
        return scheduleRepository.findAll();
    }

    public Schedule findScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new CustomException("場次不存在"));
    }

    public List<Schedule> findSchedulesByMovie(Long movieId) {
        validateMovie(movieId);
        return scheduleRepository.findByMovieIdAndActive(movieId, true);
    }

    public List<Schedule> findAvailableSchedules() {
        return scheduleRepository.findAvailableSchedules(LocalDateTime.now());
    }

    public List<Schedule> findAvailableSchedulesByMovie(Long movieId) {
        validateMovie(movieId);
        return scheduleRepository.findAvailableSchedulesByMovie(movieId, LocalDateTime.now());
    }

    public List<Schedule> findByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        validateDateRange(startTime, endTime);
        return scheduleRepository.findByDateRange(startTime, endTime);
    }

    public List<Schedule> findByMovieIdAndDateRange(Long movieId, LocalDateTime startTime, LocalDateTime endTime) {
        validateMovie(movieId);
        validateDateRange(startTime, endTime);
        return scheduleRepository.findByMovieIdAndDateRange(movieId, startTime, endTime);
    }

    public List<Schedule> findByHallAndDateRange(String hall, LocalDateTime startTime, LocalDateTime endTime) {
        validateDateRange(startTime, endTime);
        return scheduleRepository.findByHallAndShowTimeBetween(hall, startTime, endTime);
    }

    public Long countSchedulesByMovieAndDateRange(Long movieId, LocalDateTime startTime, LocalDateTime endTime) {
        validateMovie(movieId);
        validateDateRange(startTime, endTime);
        return scheduleRepository.countSchedulesByMovieAndDateRange(movieId, startTime, endTime);
    }

    // 座位相關操作
    @Transactional
    public Schedule updateAvailableSeats(Long id, int seatsToBook) {
        Schedule schedule = findScheduleById(id);
        validateSeatBooking(schedule, seatsToBook);

        schedule.setAvailableSeats(schedule.getAvailableSeats() - seatsToBook);
        schedule.setUpdatedAt(LocalDateTime.now());
        return scheduleRepository.save(schedule);
    }

    @Transactional
    public Schedule resetAvailableSeats(Long id) {
        Schedule schedule = findScheduleById(id);
        schedule.setAvailableSeats(schedule.getTotalSeats());
        schedule.setUpdatedAt(LocalDateTime.now());
        return scheduleRepository.save(schedule);
    }

    // 場次管理
    @Transactional
    public Schedule createSchedule(Schedule schedule) {
        validateSchedule(schedule);
        LocalDateTime now = LocalDateTime.now();
        schedule.setCreatedAt(now);
        schedule.setUpdatedAt(now);
        schedule.setActive(true);
        return scheduleRepository.save(schedule);
    }

    @Transactional
    public Schedule updateSchedule(Long id, Schedule schedule) {
        Schedule existingSchedule = findScheduleById(id);
        validateSchedule(schedule);

        existingSchedule.setShowTime(schedule.getShowTime());
        existingSchedule.setHall(schedule.getHall());
        existingSchedule.setTotalSeats(schedule.getTotalSeats());
        existingSchedule.setAvailableSeats(schedule.getAvailableSeats());
        existingSchedule.setActive(schedule.isActive());
        existingSchedule.setUpdatedAt(LocalDateTime.now());

        return scheduleRepository.save(existingSchedule);
    }

    @Transactional
    public void deleteSchedule(Long id) {
        Schedule schedule = findScheduleById(id);
        scheduleRepository.delete(schedule);
    }

    // 定時任務
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void cleanupExpiredSchedules() {
        List<Schedule> expiredSchedules = scheduleRepository.findByShowTimeBefore(LocalDateTime.now());
        expiredSchedules.forEach(schedule -> {
            schedule.setActive(false);
            schedule.setUpdatedAt(LocalDateTime.now());
        });
        scheduleRepository.saveAll(expiredSchedules);
    }

    // 驗證方法
    private void validateMovie(Long movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new CustomException("電影不存在");
        }
    }

    private void validateSchedule(Schedule schedule) {
        if (schedule.getShowTime() == null) {
            throw new CustomException("放映時間不能為空");
        }

        if (schedule.getShowTime().isBefore(LocalDateTime.now())) {
            throw new CustomException("放映時間不能早於現在");
        }

        Optional<Movie> movie = movieRepository.findById(schedule.getMovieId());
        if (movie.isEmpty()) {
            throw new CustomException("電影不存在");
        }

        Movie movieEntity = movie.get();
        if (schedule.getShowTime().isAfter(movieEntity.getEndDate())) {
            throw new CustomException("場次時間超出電影下架時間");
        }

        validateSeats(schedule);
        validateScheduleConflict(schedule);
    }

    private void validateSeats(Schedule schedule) {
        if (schedule.getTotalSeats() <= 0) {
            throw new CustomException("總座位數必須大於0");
        }

        if (schedule.getTotalSeats() > 300) {
            throw new CustomException("總座位數不能超過300");
        }

        if (schedule.getAvailableSeats() > schedule.getTotalSeats()) {
            throw new CustomException("可用座位數不能大於總座位數");
        }
    }

    private void validateScheduleConflict(Schedule schedule) {
        List<Schedule> conflictSchedules = scheduleRepository.findByHallAndShowTimeBetween(
                schedule.getHall(),
                schedule.getShowTime().minusHours(3),
                schedule.getShowTime().plusHours(3)
        );

        if (!conflictSchedules.isEmpty()) {
            throw new CustomException("該影廳在此時段已有其他場次安排");
        }
    }

    private void validateSeatBooking(Schedule schedule, int seatsToBook) {
        if (seatsToBook <= 0) {
            throw new CustomException("訂票數量必須大於0");
        }

        if (schedule.getAvailableSeats() < seatsToBook) {
            throw new CustomException("可用座位數不足");
        }

        if (!schedule.isActive()) {
            throw new CustomException("此場次已關閉");
        }

        if (schedule.getShowTime().isBefore(LocalDateTime.now())) {
            throw new CustomException("此場次已過期");
        }
    }

    private void validateDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new CustomException("起始時間和結束時間不能為空");
        }

        if (startTime.isAfter(endTime)) {
            throw new CustomException("起始時間不能晚於結束時間");
        }
    }
}
