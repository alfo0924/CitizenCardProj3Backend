//package org.example._citizencard3.service;
//
//import lombok.RequiredArgsConstructor;
//import org.example._citizencard3.exception.CustomException;
//import org.example._citizencard3.model.Movie;
//import org.example._citizencard3.model.Schedule;
//import org.example._citizencard3.repository.MovieRepository;
//import org.example._citizencard3.repository.ScheduleRepository;
//import org.example._citizencard3.service.ScheduleService;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Service
////@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class ScheduleServiceImpl extends ScheduleService {
//
//    private final ScheduleRepository scheduleRepository;
//    private final MovieRepository movieRepository;
//
//    public ScheduleServiceImpl(ScheduleRepository scheduleRepository, MovieRepository movieRepository, ScheduleRepository scheduleRepository1, MovieRepository movieRepository1) {
//        super(scheduleRepository, movieRepository);
//        this.scheduleRepository = scheduleRepository1;
//        this.movieRepository = movieRepository1;
//    }
//
//    @Override
//    public List<Schedule> findAllSchedules() {
//        return scheduleRepository.findAll();
//    }
//
//    @Override
//    public Schedule findScheduleById(Long id) {
//        return scheduleRepository.findById(id)
//                .orElseThrow(() -> new CustomException("場次不存在", HttpStatus.NOT_FOUND));
//    }
//
//    @Override
//    public List<Schedule> findSchedulesByMovie(Long movieId) {
//        validateMovie(movieId);
//        return scheduleRepository.findByMovieId(movieId);
//    }
//
//    @Override
//    public List<Schedule> findByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
//        validateDateRange(startTime, endTime);
//        return scheduleRepository.findByShowTimeBetween(startTime, endTime);
//    }
//
//    @Override
//    public List<Schedule> findByMovieIdAndDateRange(Long movieId, LocalDateTime startTime, LocalDateTime endTime) {
//        validateMovie(movieId);
//        validateDateRange(startTime, endTime);
//        return scheduleRepository.findByMovieIdAndShowTimeBetween(movieId, startTime, endTime);
//    }
//
//    @Override
//    public List<Schedule> findByHallAndDateRange(String hall, LocalDateTime startTime, LocalDateTime endTime) {
//        validateDateRange(startTime, endTime);
//        return scheduleRepository.findByHallAndShowTimeBetween(hall, startTime, endTime);
//    }
//
//    @Override
//    @Transactional
//    public Schedule updateAvailableSeats(Long id, int seatsToBook) {
//        Schedule schedule = findScheduleById(id);
//        validateSeatBooking(schedule, seatsToBook);
//
//        schedule.setAvailableSeats(schedule.getAvailableSeats() - seatsToBook);
//        schedule.setUpdatedAt(LocalDateTime.now());
//        return scheduleRepository.save(schedule);
//    }
//
//    @Override
//    @Transactional
//    public Schedule createSchedule(Schedule schedule) {
//        validateSchedule(schedule);
//
//        LocalDateTime now = LocalDateTime.now();
//        schedule.setCreatedAt(now);
//        schedule.setUpdatedAt(now);
//        schedule.setActive(true);
//
//        return scheduleRepository.save(schedule);
//    }
//
//    private void validateMovie(Long movieId) {
//        if (!movieRepository.existsById(movieId)) {
//            throw new CustomException("電影不存在", HttpStatus.NOT_FOUND);
//        }
//    }
//
//    private void validateSchedule(Schedule schedule) {
//        if (schedule.getMovieId() == null) {
//            throw new CustomException("電影ID不能為空", HttpStatus.BAD_REQUEST);
//        }
//        if (schedule.getShowTime() == null) {
//            throw new CustomException("放映時間不能為空", HttpStatus.BAD_REQUEST);
//        }
//        if (schedule.getHall() == null || schedule.getHall().trim().isEmpty()) {
//            throw new CustomException("影廳不能為空", HttpStatus.BAD_REQUEST);
//        }
//        if (schedule.getTotalSeats() == null || schedule.getTotalSeats() <= 0) {
//            throw new CustomException("總座位數必須大於0", HttpStatus.BAD_REQUEST);
//        }
//        if (schedule.getAvailableSeats() == null || schedule.getAvailableSeats() > schedule.getTotalSeats()) {
//            throw new CustomException("可用座位數不正確", HttpStatus.BAD_REQUEST);
//        }
//
//        validateMovie(schedule.getMovieId());
//        validateShowTime(schedule);
//    }
//
//    private void validateShowTime(Schedule schedule) {
//        if (schedule.getShowTime().isBefore(LocalDateTime.now())) {
//            throw new CustomException("放映時間不能早於現在", HttpStatus.BAD_REQUEST);
//        }
//    }
//
//    private void validateSeatBooking(Schedule schedule, int seatsToBook) {
//        if (!schedule.getActive()) {
//            throw new CustomException("此場次已關閉", HttpStatus.BAD_REQUEST);
//        }
//        if (schedule.getShowTime().isBefore(LocalDateTime.now())) {
//            throw new CustomException("此場次已過期", HttpStatus.BAD_REQUEST);
//        }
//        if (seatsToBook <= 0) {
//            throw new CustomException("訂票數量必須大於0", HttpStatus.BAD_REQUEST);
//        }
//        if (schedule.getAvailableSeats() < seatsToBook) {
//            throw new CustomException("可用座位數不足", HttpStatus.BAD_REQUEST);
//        }
//    }
//
//    private void validateDateRange(LocalDateTime startTime, LocalDateTime endTime) {
//        if (startTime == null || endTime == null) {
//            throw new CustomException("起始時間和結束時間不能為空", HttpStatus.BAD_REQUEST);
//        }
//        if (startTime.isAfter(endTime)) {
//            throw new CustomException("起始時間不能晚於結束時間", HttpStatus.BAD_REQUEST);
//        }
//    }
//}
