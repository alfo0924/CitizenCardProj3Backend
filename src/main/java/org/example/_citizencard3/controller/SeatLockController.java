package org.example._citizencard3.controller;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.dto.request.SeatLockRequest;
import org.example._citizencard3.dto.response.SeatLockResponse;
import org.example._citizencard3.dto.response.SeatStatusResponse;
import org.example._citizencard3.repository.MovieTicketRepository;
import org.example._citizencard3.service.SeatLockService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.example._citizencard3.repository.UserRepository;
import org.example._citizencard3.model.User;
import org.example._citizencard3.exception.CustomException;
import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
@CrossOrigin(origins = {"${app.cors.allowed-origins}"})
public class SeatLockController {
  private final SeatLockService seatLockService;
  private final UserRepository userRepository;
  private final MovieTicketRepository movieTicketRepository;


  private Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new CustomException("未登入", HttpStatus.UNAUTHORIZED);
    }
    String email = authentication.getName();
    return userRepository.findByEmail(email)
        .map(User::getId)
        .orElseThrow(() -> new CustomException("無效的用戶ID", HttpStatus.BAD_REQUEST));
  }

  // 獲取場次所有座位狀態
  @GetMapping("/{scheduleId}/status")
  public ResponseEntity<List<SeatStatusResponse>> getAllSeatsStatus(
      @PathVariable Long scheduleId) {
    return ResponseEntity.ok(seatLockService.getAllSeatsStatus(scheduleId));
  }

  // 獲取座位是否已售出
  @GetMapping("/{scheduleId}/sold")
  public ResponseEntity<List<String>> getSoldSeats(@PathVariable Long scheduleId) {
    List<String> soldSeats = movieTicketRepository.findBookedSeatsByScheduleId(scheduleId);
    return ResponseEntity.ok(soldSeats);
  }

  // 鎖定座位
  @PostMapping("/lock")
  public ResponseEntity<SeatLockResponse> lockSeat(@Valid @RequestBody SeatLockRequest request) {
    Long userId = getCurrentUserId();
    return ResponseEntity.ok(seatLockService.lockSeat(request, userId));
  }

  // 釋放座位鎖定
  @PostMapping("/release")
  public ResponseEntity<Boolean> releaseLock(@Valid @RequestBody SeatLockRequest request) {
    Long userId = getCurrentUserId();
    return ResponseEntity.ok(seatLockService.releaseLock(
        request.getScheduleId(),
        request.getSeatNumber(),
        userId
    ));
  }
}