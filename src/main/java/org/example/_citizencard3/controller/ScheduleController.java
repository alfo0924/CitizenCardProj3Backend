package org.example._citizencard3.controller;

import org.example._citizencard3.dto.request.ScheduleRequest;
import org.example._citizencard3.dto.response.ScheduleResponse;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.mapper.ScheduleMapper;
import org.example._citizencard3.model.Schedule;
import org.example._citizencard3.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 電影場次管理控制器
 * @author Your Name
 * @version 1.0
 */
@RestController
@RequestMapping("/schedules")
//@CrossOrigin(origins = "*")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ScheduleMapper scheduleMapper;

    /**
     * 獲取所有電影場次
     * @return 返回所有場次列表
     */
    @GetMapping("")
    public ResponseEntity<?> getAllSchedules() {
        try {
            List<Schedule> schedules = scheduleService.findAllSchedules();
            return ResponseEntity.ok(scheduleMapper.toResponseList(schedules));
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "獲取排程列表失敗");
            response.put("status", 500);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 根據ID獲取特定場次
     * @param id 場次ID
     * @return 返回指定場次詳情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getScheduleById(@PathVariable Long id) {
        try {
            Schedule schedule = scheduleService.findScheduleById(id);
            ScheduleResponse response = scheduleMapper.toResponse(schedule);
            response.calculateSeatsInfo();
            response.calculateShowStatus();
            return ResponseEntity.ok(response);
        } catch (CustomException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            response.put("status", e.getStatus().value());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(e.getStatus()).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "獲取排程詳情失敗");
            response.put("status", 500);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 根據電影ID獲取相關場次
     * @param movieId 電影ID
     * @return 返回指定電影的所有場次
     */
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<?> getSchedulesByMovie(@PathVariable Long movieId) {
        try {
            List<Schedule> schedules = scheduleService.findSchedulesByMovie(movieId);
            return ResponseEntity.ok(scheduleMapper.toResponseList(schedules));
        } catch (CustomException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            response.put("status", e.getStatus().value());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(e.getStatus()).body(response);
        }
    }

    /**
     * 獲取所有可用場次
     * @return 返回所有可預訂的場次
     */
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableSchedules() {
        try {
            List<Schedule> schedules = scheduleService.findAvailableSchedules();
            return ResponseEntity.ok(scheduleMapper.toResponseList(schedules));
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "獲取可用排程失敗");
            response.put("status", 500);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 獲取指定電影的可用場次
     * @param movieId 電影ID
     * @return 返回指定電影的可預訂場次
     */
    @GetMapping("/movie/{movieId}/available")
    public ResponseEntity<?> getAvailableSchedulesByMovie(@PathVariable Long movieId) {
        try {
            List<Schedule> schedules = scheduleService.findAvailableSchedulesByMovie(movieId);
            return ResponseEntity.ok(scheduleMapper.toResponseList(schedules));
        } catch (CustomException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            response.put("status", e.getStatus().value());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(e.getStatus()).body(response);
        }
    }

    /**
     * 獲取指定日期範圍內的場次
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return 返回指定時間範圍內的場次
     */
    @GetMapping("/date-range")
    public ResponseEntity<?> getSchedulesByDateRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            List<Schedule> schedules = scheduleService.findByDateRange(startTime, endTime);
            return ResponseEntity.ok(scheduleMapper.toResponseList(schedules));
        } catch (CustomException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            response.put("status", e.getStatus().value());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(e.getStatus()).body(response);
        }
    }

    /**
     * 更新場次座位數量（訂票使用）
     * @param id 場次ID
     * @param seatsToBook 預訂座位數
     * @return 返回更新後的場次資訊
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{id}/seats")
    public ResponseEntity<?> updateAvailableSeats(
        @PathVariable Long id,
        @RequestParam int seatsToBook) {
        try {
            Schedule schedule = scheduleService.updateAvailableSeats(id, seatsToBook);
            return ResponseEntity.ok(scheduleMapper.toResponse(schedule));
        } catch (CustomException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            response.put("status", e.getStatus().value());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(e.getStatus()).body(response);
        }
    }

    /**
     * 統計指定電影在日期範圍內的場次數量
     * @param movieId 電影ID
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return 返回場次統計數量
     */
    @GetMapping("/movie/{movieId}/count")
    public ResponseEntity<?> countSchedulesByMovie(
        @PathVariable Long movieId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            Long count = scheduleService.countSchedulesByMovieAndDateRange(movieId, startTime, endTime);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (CustomException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            response.put("status", e.getStatus().value());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(e.getStatus()).body(response);
        }
    }

    /**
     * 建立新場次（管理員使用）
     * @param request 場次請求資料
     * @return 返回新建的場次資訊
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<?> createSchedule(@Valid @RequestBody ScheduleRequest request) {
        try {
            Schedule schedule = scheduleService.createSchedule(scheduleMapper.toEntity(request));
            return ResponseEntity.ok(scheduleMapper.toResponse(schedule));
        } catch (CustomException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            response.put("status", e.getStatus().value());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(e.getStatus()).body(response);
        }
    }

    /**
     * 更新場次資訊（管理員使用）
     * @param id 場次ID
     * @param request 更新的場次資料
     * @return 返回更新後的場次資訊
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSchedule(
        @PathVariable Long id,
        @Valid @RequestBody ScheduleRequest request) {
        try {
            Schedule schedule = scheduleService.updateSchedule(id, scheduleMapper.toEntity(request));
            return ResponseEntity.ok(scheduleMapper.toResponse(schedule));
        } catch (CustomException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            response.put("status", e.getStatus().value());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(e.getStatus()).body(response);
        }
    }

    /**
     * 刪除場次（管理員使用）
     * @param id 場次ID
     * @return 返回刪除成功訊息
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
        try {
            scheduleService.deleteSchedule(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "場次刪除成功");
            response.put("status", 200);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (CustomException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            response.put("status", e.getStatus().value());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(e.getStatus()).body(response);
        }
    }
}