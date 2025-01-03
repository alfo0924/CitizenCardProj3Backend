package org.example._citizencard3.controller;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.model.MovieTicket;
import org.example._citizencard3.model.DiscountCoupon;
import org.example._citizencard3.model.Wallet;
import org.example._citizencard3.model.User;
import org.example._citizencard3.repository.MovieTicketRepository;
import org.example._citizencard3.repository.DiscountCouponRepository;
import org.example._citizencard3.repository.UserRepository;
import org.example._citizencard3.service.WalletService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
@CrossOrigin(origins = {"${app.cors.allowed-origins}"})
public class WalletController {

    private final WalletService walletService;
    private final MovieTicketRepository movieTicketRepository;
    private final DiscountCouponRepository discountCouponRepository;
    private final UserRepository userRepository;

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
            @RequestParam(defaultValue = "createdAt,DESC") String sort) {
        try {
            String[] sortParams = sort.split(",");
            Sort.Direction direction = Sort.Direction.valueOf(sortParams[1].toUpperCase());
            Sort sortObj = Sort.by(direction, sortParams[0]);
            return ResponseEntity.ok(walletService.getMovieTickets(getCurrentUserId(),
                    PageRequest.of(page, size, sortObj)));
        } catch (IllegalArgumentException e) {
            throw new CustomException("無效的排序參數", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/tickets/valid")
    public ResponseEntity<Page<MovieTicket>> getValidMovieTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(walletService.getValidMovieTickets(getCurrentUserId(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/coupons")
    public ResponseEntity<Page<DiscountCoupon>> getDiscountCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "expiryDate,ASC") String sort) {
        try {
            String[] sortParams = sort.split(",");
            Sort.Direction direction = Sort.Direction.valueOf(sortParams[1].toUpperCase());
            Sort sortObj = Sort.by(direction, sortParams[0]);
            return ResponseEntity.ok(walletService.getDiscountCoupons(getCurrentUserId(),
                    PageRequest.of(page, size, sortObj)));
        } catch (IllegalArgumentException e) {
            throw new CustomException("無效的排序參數", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/coupons/valid")
    public ResponseEntity<Page<DiscountCoupon>> getValidDiscountCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(walletService.getValidDiscountCoupons(getCurrentUserId(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "expiryDate"))));
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<MovieTicket> getTicketById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        MovieTicket ticket = movieTicketRepository.findById(id)
                .orElseThrow(() -> new CustomException("找不到電影票", HttpStatus.NOT_FOUND));
        if (!ticket.getUser().getId().equals(userId)) {
            throw new CustomException("無權查看此電影票", HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/coupons/{id}")
    public ResponseEntity<DiscountCoupon> getCouponById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        DiscountCoupon coupon = discountCouponRepository.findById(id)
                .orElseThrow(() -> new CustomException("找不到優惠券", HttpStatus.NOT_FOUND));
        if (!coupon.getUser().getId().equals(userId)) {
            throw new CustomException("無權查看此優惠券", HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(coupon);
    }

    @PostMapping("/deposit")
    public ResponseEntity<Wallet> deposit(@RequestParam Double amount) {
        if (amount == null || amount <= 0) {
            throw new CustomException("儲值金額必須大於0", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(walletService.deposit(getCurrentUserId(), amount));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Wallet> withdraw(@RequestParam Double amount) {
        if (amount == null || amount <= 0) {
            throw new CustomException("提領金額必須大於0", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(walletService.withdraw(getCurrentUserId(), amount));
    }

    @PatchMapping("/tickets/{id}/use")
    public ResponseEntity<MovieTicket> useTicket(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.useMovieTicket(getCurrentUserId(), id));
    }

    @PatchMapping("/coupons/{id}/use")
    public ResponseEntity<DiscountCoupon> useCoupon(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.useDiscountCoupon(getCurrentUserId(), id));
    }

    @GetMapping("/transactions/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentTransactions(
            @RequestParam(defaultValue = "5") int limit) {
        if (limit <= 0) {
            throw new CustomException("限制數量必須大於0", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(walletService.getRecentTransactions(limit));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Double>> getWalletStats() {
        return ResponseEntity.ok(Map.of(
                "totalBalance", walletService.sumBalance(),
                "averageBalance", walletService.averageBalance()
        ));
    }
}
