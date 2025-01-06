package org.example._citizencard3.controller;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example._citizencard3.dto.response.MovieTicketResponse;
import org.example._citizencard3.repository.MovieTicketRepository;
import org.example._citizencard3.repository.UserRepository;
import org.example._citizencard3.security.UserDetailsServiceImpl;
import org.example._citizencard3.service.MovieTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 電影票管理控制器
 */
@RestController
@RequestMapping("/movietickets")
@RequiredArgsConstructor
public class MovieTicketController {
  private final MovieTicketService movieTicketService;
  private final UserRepository userRepository;
  @Autowired
  private MovieTicketRepository movieTicketRepository;



  @PostMapping("/create")
  public ResponseEntity<?> createTicket(@RequestBody Map<String, Object> requestBody) {
    try {
      Long movieId = Long.parseLong(requestBody.get("movieId").toString());
      Long scheduleId = Long.parseLong(requestBody.get("scheduleId").toString());
      String seatNumber = requestBody.get("seatNumber").toString();
      Long userId = getCurrentUserId();
      MovieTicketResponse ticket = movieTicketService.createTicket(userId, movieId, scheduleId, seatNumber);
      return ResponseEntity.ok(ticket);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("message", "建立電影票失敗: " + e.getMessage());
      response.put("status", 500);
      response.put("timestamp", System.currentTimeMillis());
      return ResponseEntity.status(500).body(response);
    }
  }

  @GetMapping("/user")
  @ResponseBody
  public ResponseEntity<?> getUserTickets() {
    try {
      Long userId = getCurrentUserId();
      List<MovieTicketResponse> tickets = movieTicketService.getUserTickets(userId);
      return ResponseEntity.ok(tickets);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("message", "獲取電影票清單失敗");
      response.put("status", 500);
      response.put("timestamp", System.currentTimeMillis());
      return ResponseEntity.status(500).body(response);
    }
  }

  @GetMapping("/check/{scheduleId}")
  @ResponseBody
  public ResponseEntity<?> checkBooking(@PathVariable Long scheduleId) {
    try {
      Long userId = getCurrentUserId();
      boolean hasBooked = movieTicketRepository.hasUserBookedSchedule(userId, scheduleId);
      Map<String, Boolean> response = new HashMap<>();
      response.put("hasBooked", hasBooked);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(500).body("檢查訂票失敗: " + e.getMessage());
    }
  }

  private Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    org.example._citizencard3.model.User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("找不到使用者"));
    return user.getId();
  }

  @PostMapping("/cancel/{ticketId}")
  public ResponseEntity<?> cancelTicket(@PathVariable Long ticketId) {
    try {
      Long userId = getCurrentUserId();
      MovieTicketResponse ticket = movieTicketService.cancelTicket(userId, ticketId);
      return ResponseEntity.ok(ticket);
    } catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("message", "取消訂票失敗: " + e.getMessage());
      response.put("status", 500);
      response.put("timestamp", System.currentTimeMillis());
      return ResponseEntity.status(500).body(response);
    }
  }
}