package org.example._citizencard3.controller;

import org.example._citizencard3.model.Schedule;
import org.example._citizencard3.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@CrossOrigin(origins = "*")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<List<Schedule>> getAllSchedules() {
        return ResponseEntity.ok(scheduleService.findAllSchedules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Schedule> getScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.findScheduleById(id));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Schedule>> getSchedulesByMovie(@PathVariable Long movieId) {
        return ResponseEntity.ok(scheduleService.findSchedulesByMovie(movieId));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Schedule>> getAvailableSchedules() {
        return ResponseEntity.ok(scheduleService.findAvailableSchedules());
    }

    @GetMapping("/movie/{movieId}/available")
    public ResponseEntity<List<Schedule>> getAvailableSchedulesByMovie(@PathVariable Long movieId) {
        return ResponseEntity.ok(scheduleService.findAvailableSchedulesByMovie(movieId));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Schedule>> getSchedulesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(scheduleService.findByDateRange(startTime, endTime));
    }

    @GetMapping("/movie/{movieId}/date-range")
    public ResponseEntity<List<Schedule>> getSchedulesByMovieAndDateRange(
            @PathVariable Long movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(scheduleService.findByMovieIdAndDateRange(movieId, startTime, endTime));
    }

    @GetMapping("/hall/{hall}/date-range")
    public ResponseEntity<List<Schedule>> getSchedulesByHallAndDateRange(
            @PathVariable String hall,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(scheduleService.findByHallAndDateRange(hall, startTime, endTime));
    }

    @PostMapping("/update-seats/{id}")
    public ResponseEntity<Schedule> updateAvailableSeats(
            @PathVariable Long id,
            @RequestParam int seatsToBook) {
        return ResponseEntity.ok(scheduleService.updateAvailableSeats(id, seatsToBook));
    }

    @GetMapping("/count/movie/{movieId}/date-range")
    public ResponseEntity<Long> countSchedulesByMovieAndDateRange(
            @PathVariable Long movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(scheduleService.countSchedulesByMovieAndDateRange(movieId, startTime, endTime));
    }
}
