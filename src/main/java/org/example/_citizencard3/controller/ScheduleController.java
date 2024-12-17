package org.example._citizencard3.controller;

import org.example._citizencard3.model.Schedule;
import org.example._citizencard3.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping({"/api/schedules", "/api/schedule"}) // 支援複數和單數形式
@CrossOrigin(origins = "*")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping({"", "/"}) // 支援根路徑訪問
    public ResponseEntity<List<Schedule>> getAllSchedules() {
        return ResponseEntity.ok(scheduleService.findAllSchedules());
    }

    @GetMapping({"/{id}", "/detail/{id}"}) // 支援多種ID查詢路徑
    public ResponseEntity<Schedule> getScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.findScheduleById(id));
    }

    @GetMapping({"/movie/{movieId}", "/film/{movieId}"}) // 支援多種電影查詢路徑
    public ResponseEntity<List<Schedule>> getSchedulesByMovie(@PathVariable Long movieId) {
        return ResponseEntity.ok(scheduleService.findSchedulesByMovie(movieId));
    }

    @GetMapping({"/available", "/active"}) // 支援多種可用場次查詢路徑
    public ResponseEntity<List<Schedule>> getAvailableSchedules() {
        return ResponseEntity.ok(scheduleService.findAvailableSchedules());
    }

    @GetMapping({"/movie/{movieId}/available", "/film/{movieId}/active"}) // 支援多種可用電影場次查詢路徑
    public ResponseEntity<List<Schedule>> getAvailableSchedulesByMovie(@PathVariable Long movieId) {
        return ResponseEntity.ok(scheduleService.findAvailableSchedulesByMovie(movieId));
    }

    @GetMapping({"/date-range", "/period"}) // 支援多種日期範圍查詢路徑
    public ResponseEntity<List<Schedule>> getSchedulesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(scheduleService.findByDateRange(startTime, endTime));
    }

    @GetMapping({"/movie/{movieId}/date-range", "/film/{movieId}/period"}) // 支援多種電影日期範圍查詢路徑
    public ResponseEntity<List<Schedule>> getSchedulesByMovieAndDateRange(
            @PathVariable Long movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(scheduleService.findByMovieIdAndDateRange(movieId, startTime, endTime));
    }

    @GetMapping({"/hall/{hall}/date-range", "/theater/{hall}/period"}) // 支援多種影廳日期範圍查詢路徑
    public ResponseEntity<List<Schedule>> getSchedulesByHallAndDateRange(
            @PathVariable String hall,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(scheduleService.findByHallAndDateRange(hall, startTime, endTime));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping({"/update-seats/{id}", "/book-seats/{id}"}) // 支援多種座位更新路徑
    public ResponseEntity<Schedule> updateAvailableSeats(
            @PathVariable Long id,
            @RequestParam int seatsToBook) {
        return ResponseEntity.ok(scheduleService.updateAvailableSeats(id, seatsToBook));
    }

    @GetMapping({"/count/movie/{movieId}/date-range", "/count/film/{movieId}/period"}) // 支援多種計數查詢路徑
    public ResponseEntity<Long> countSchedulesByMovieAndDateRange(
            @PathVariable Long movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(scheduleService.countSchedulesByMovieAndDateRange(movieId, startTime, endTime));
    }
}
