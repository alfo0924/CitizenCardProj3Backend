package org.example._citizencard3.controller;

import org.example._citizencard3.dto.response.ScheduleResponse;
import org.example._citizencard3.model.Schedule;
import org.example._citizencard3.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/schedules")
    public ResponseEntity<?> getAllSchedules() {
        try {
            List<Schedule> schedules = scheduleService.findAllSchedules();
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "獲取排程列表失敗");
            response.put("status", 500);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/schedules/{id}")
    public ResponseEntity<?> getScheduleById(@PathVariable Long id) {
        try {
            Schedule schedule = scheduleService.findScheduleById(id);
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "獲取排程詳情失敗");
            response.put("status", 500);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/schedules/movie/{movieId}")
    public ResponseEntity<?> getSchedulesByMovie(@PathVariable Long movieId) {
        try {
            List<Schedule> schedules = scheduleService.findSchedulesByMovie(movieId);
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "獲取電影排程失敗");
            response.put("status", 500);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/schedules/available")
    public ResponseEntity<?> getAvailableSchedules() {
        try {
            List<Schedule> schedules = scheduleService.findAvailableSchedules();
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "獲取可用排程失敗");
            response.put("status", 500);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/schedules/movie/{movieId}/available")
    public ResponseEntity<?> getAvailableSchedulesByMovie(@PathVariable Long movieId) {
        try {
            List<Schedule> schedules = scheduleService.findAvailableSchedulesByMovie(movieId);
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "獲取電影可用排程失敗");
            response.put("status", 500);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/schedules/date-range")
    public ResponseEntity<?> getSchedulesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            List<Schedule> schedules = scheduleService.findByDateRange(startTime, endTime);
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "獲取日期範圍排程失敗");
            response.put("status", 500);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/schedules/{id}/seats")
    public ResponseEntity<?> updateAvailableSeats(
            @PathVariable Long id,
            @RequestParam int seatsToBook) {
        try {
            Schedule schedule = scheduleService.updateAvailableSeats(id, seatsToBook);
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "更新座位數量失敗");
            response.put("status", 500);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/schedules/movie/{movieId}/count")
    public ResponseEntity<?> countSchedulesByMovie(
            @PathVariable Long movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            Long count = scheduleService.countSchedulesByMovieAndDateRange(movieId, startTime, endTime);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "獲取排程數量失敗");
            response.put("status", 500);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }
}
