package org.example._citizencard3.controller;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.model.MovieTicket;
import org.example._citizencard3.model.DiscountCoupon;
import org.example._citizencard3.model.Wallet;
import org.example._citizencard3.repository.MovieTicketRepository;
import org.example._citizencard3.repository.DiscountCouponRepository;
import org.example._citizencard3.service.WalletService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:3009"})
public class WalletController {

    private final WalletService walletService;
    private final MovieTicketRepository movieTicketRepository;
    private final DiscountCouponRepository discountCouponRepository;

    private Long getCurrentUserId() {
        // TODO: Implement getting current user ID from security context
        return 1L;
    }

    @GetMapping("/info")
    public ResponseEntity<Wallet> getWalletInfo() {
        return ResponseEntity.ok(walletService.getWalletByUserId(getCurrentUserId()));
    }

    @GetMapping("/balance")
    public ResponseEntity<Double> getWalletBalance() {
        return ResponseEntity.ok(walletService.getBalance(getCurrentUserId()));
    }

    @GetMapping("/tickets")
    public ResponseEntity<Page<MovieTicket>> getMovieTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return ResponseEntity.ok(walletService.getMovieTickets(getCurrentUserId(),
                PageRequest.of(page, size, Sort.by(sort.split(",")))));
    }

    @GetMapping("/coupons")
    public ResponseEntity<Page<DiscountCoupon>> getDiscountCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "expiryDate,asc") String sort) {
        return ResponseEntity.ok(walletService.getDiscountCoupons(getCurrentUserId(),
                PageRequest.of(page, size, Sort.by(sort.split(",")))));
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<MovieTicket> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(movieTicketRepository.findById(id)
                .orElseThrow(() -> new CustomException("找不到電影票", HttpStatus.NOT_FOUND)));
    }

    @GetMapping("/coupons/{id}")
    public ResponseEntity<DiscountCoupon> getCouponById(@PathVariable Long id) {
        return ResponseEntity.ok(discountCouponRepository.findById(id)
                .orElseThrow(() -> new CustomException("找不到優惠券", HttpStatus.NOT_FOUND)));
    }

    @PostMapping("/deposit")
    public ResponseEntity<Wallet> deposit(@RequestParam Double amount) {
        return ResponseEntity.ok(walletService.deposit(getCurrentUserId(), amount));
    }

    @PatchMapping("/tickets/{id}/use")
    public ResponseEntity<MovieTicket> useTicket(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.useMovieTicket(getCurrentUserId(), id));
    }

    @PatchMapping("/coupons/{id}/use")
    public ResponseEntity<DiscountCoupon> useCoupon(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.useDiscountCoupon(getCurrentUserId(), id));
    }
}