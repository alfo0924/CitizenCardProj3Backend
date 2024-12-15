package org.example._citizencard3.controller;

import org.example._citizencard3.service.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qrcode")
public class QRCodeController {

    @Autowired
    private QRCodeService qrCodeService;

    // 驗證電影票QR碼
    @PostMapping("/movie-ticket/validate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> validateMovieTicketQRCode(
            @RequestParam String qrCodeData,
            @RequestParam Long ticketId) {
        boolean isValid = qrCodeService.validateMovieTicketQRCode(qrCodeData, ticketId);
        return ResponseEntity.ok().body(isValid);
    }

    // 驗證優惠券QR碼
    @PostMapping("/discount-coupon/validate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> validateDiscountCouponQRCode(
            @RequestParam String qrCodeData,
            @RequestParam Long couponId) {
        boolean isValid = qrCodeService.validateDiscountCouponQRCode(qrCodeData, couponId);
        return ResponseEntity.ok().body(isValid);
    }

    // 取得電影票QR碼
    @GetMapping("/movie-ticket/{ticketId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMovieTicketQRCode(@PathVariable Long ticketId) {
        return ResponseEntity.ok().body(qrCodeService.getMovieTicketQRCode(ticketId));
    }

    // 取得優惠券QR碼
    @GetMapping("/discount-coupon/{couponId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getDiscountCouponQRCode(@PathVariable Long couponId) {
        return ResponseEntity.ok().body(qrCodeService.getDiscountCouponQRCode(couponId));
    }

    // 標記電影票QR碼已使用
    @PostMapping("/movie-ticket/{ticketId}/use")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> useMovieTicketQRCode(@PathVariable Long ticketId) {
        qrCodeService.markMovieTicketQRCodeAsUsed(String.valueOf(ticketId));
        return ResponseEntity.ok().build();
    }

    // 標記優惠券QR碼已使用
    @PostMapping("/discount-coupon/{couponId}/use")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> useDiscountCouponQRCode(@PathVariable Long couponId) {
        qrCodeService.markDiscountCouponQRCodeAsUsed(String.valueOf(couponId));
        return ResponseEntity.ok().build();
    }

    // 重新生成電影票QR碼
    @PostMapping("/movie-ticket/{ticketId}/regenerate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> regenerateMovieTicketQRCode(@PathVariable Long ticketId) {
        return ResponseEntity.ok().body(qrCodeService.regenerateMovieTicketQRCode(ticketId));
    }

    // 重新生成優惠券QR碼
    @PostMapping("/discount-coupon/{couponId}/regenerate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> regenerateDiscountCouponQRCode(@PathVariable Long couponId) {
        return ResponseEntity.ok().body(qrCodeService.regenerateDiscountCouponQRCode(couponId));
    }
}
