package org.example._citizencard3.controller;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.dto.response.DiscountCouponResponse;
import org.example._citizencard3.dto.response.MovieTicketResponse;
import org.example._citizencard3.service.DiscountCouponService;
import org.example._citizencard3.service.MovieTicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class WalletController {

    private final MovieTicketService movieTicketService;
    private final DiscountCouponService discountCouponService;

    // 獲取用戶的所有票券
    @GetMapping("/tickets")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getTickets(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();

        List<MovieTicketResponse> movieTickets = movieTicketService.getCurrentUserTickets();
        List<DiscountCouponResponse> discountCoupons = discountCouponService.getCurrentUserCoupons(userId);

        response.put("movieTickets", movieTickets);
        response.put("discountCoupons", discountCoupons);

        return ResponseEntity.ok(response);
    }

    // 獲取電影票詳情
    @GetMapping("/tickets/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MovieTicketResponse> getTicketDetail(@PathVariable Long id) {
        MovieTicketResponse ticket = movieTicketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    // 使用優惠券
    @PostMapping("/coupons/{id}/use")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DiscountCouponResponse> useCoupon(
            @RequestParam Long userId,
            @PathVariable Long id) {
        DiscountCouponResponse coupon = discountCouponService.useCoupon(userId, id);
        return ResponseEntity.ok(coupon);
    }

    // 獲取優惠券詳情
    @GetMapping("/coupons/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DiscountCouponResponse> getCouponDetail(@PathVariable Long id) {
        DiscountCouponResponse coupon = discountCouponService.getCouponById(id);
        return ResponseEntity.ok(coupon);
    }

    // 檢查優惠券有效性
    @GetMapping("/coupons/{id}/validate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> validateCoupon(
            @RequestParam Long userId,
            @PathVariable Long id) {
        boolean isValid = discountCouponService.validateCoupon(userId, id);
        return ResponseEntity.ok(isValid);
    }

    // 處理異常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}