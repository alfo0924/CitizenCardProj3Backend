package org.example._citizencard3.service;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.model.MovieTicket;
import org.example._citizencard3.model.DiscountCoupon;
import org.example._citizencard3.model.User;
import org.example._citizencard3.model.Wallet;
import org.example._citizencard3.repository.MovieTicketRepository;
import org.example._citizencard3.repository.DiscountCouponRepository;
import org.example._citizencard3.repository.UserRepository;
import org.example._citizencard3.repository.WalletRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final MovieTicketRepository movieTicketRepository;
    private final DiscountCouponRepository discountCouponRepository;

    public Wallet getWalletByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("找不到用戶", HttpStatus.NOT_FOUND));

        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException("找不到用戶錢包", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Wallet deposit(Long userId, Double amount) {
        if (amount <= 0) {
            throw new CustomException("儲值金額必須大於0", HttpStatus.BAD_REQUEST);
        }

        Wallet wallet = getWalletByUserId(userId);
        wallet.setBalance(wallet.getBalance() + amount);
        wallet.setUpdatedAt(LocalDateTime.now());
        return walletRepository.save(wallet);
    }

    @Transactional
    public Wallet withdraw(Long userId, Double amount) {
        if (amount <= 0) {
            throw new CustomException("提領金額必須大於0", HttpStatus.BAD_REQUEST);
        }

        Wallet wallet = getWalletByUserId(userId);
        if (wallet.getBalance() < amount) {
            throw new CustomException("餘額不足", HttpStatus.BAD_REQUEST);
        }

        wallet.setBalance(wallet.getBalance() - amount);
        wallet.setUpdatedAt(LocalDateTime.now());
        return walletRepository.save(wallet);
    }

    public Double getBalance(Long userId) {
        return getWalletByUserId(userId).getBalance();
    }

    public Page<MovieTicket> getMovieTickets(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("找不到用戶", HttpStatus.NOT_FOUND));
        return movieTicketRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Page<DiscountCoupon> getDiscountCoupons(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("找不到用戶", HttpStatus.NOT_FOUND));
        return discountCouponRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Page<MovieTicket> getValidMovieTickets(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("找不到用戶", HttpStatus.NOT_FOUND));
        return movieTicketRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, "VALID", pageable);
    }

    public Page<DiscountCoupon> getValidDiscountCoupons(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("找不到用戶", HttpStatus.NOT_FOUND));
        return discountCouponRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, "VALID", pageable);
    }

    @Transactional
    public MovieTicket useMovieTicket(Long userId, Long ticketId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("找不到用戶", HttpStatus.NOT_FOUND));

        MovieTicket ticket = movieTicketRepository.findById(ticketId)
                .orElseThrow(() -> new CustomException("找不到電影票", HttpStatus.NOT_FOUND));

        if (!ticket.getUser().getId().equals(userId)) {
            throw new CustomException("無權使用此電影票", HttpStatus.FORBIDDEN);
        }

        if (!"VALID".equals(ticket.getStatus())) {
            throw new CustomException("電影票已失效", HttpStatus.BAD_REQUEST);
        }

        ticket.setStatus(MovieTicket.TicketStatus.valueOf("USED"));
        ticket.setUpdatedAt(LocalDateTime.now());
        return movieTicketRepository.save(ticket);
    }

    @Transactional
    public DiscountCoupon useDiscountCoupon(Long userId, Long couponId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("找不到用戶", HttpStatus.NOT_FOUND));

        DiscountCoupon coupon = discountCouponRepository.findById(couponId)
                .orElseThrow(() -> new CustomException("找不到優惠券", HttpStatus.NOT_FOUND));

        if (!coupon.getUser().getId().equals(userId)) {
            throw new CustomException("無權使用此優惠券", HttpStatus.FORBIDDEN);
        }

        if (!"VALID".equals(coupon.getStatus())) {
            throw new CustomException("優惠券已失效", HttpStatus.BAD_REQUEST);
        }

        if (coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
            coupon.setStatus(DiscountCoupon.CouponStatus.valueOf("EXPIRED"));
            discountCouponRepository.save(coupon);
            throw new CustomException("優惠券已過期", HttpStatus.BAD_REQUEST);
        }

        coupon.setStatus(DiscountCoupon.CouponStatus.valueOf("USED"));
        coupon.setUpdatedAt(LocalDateTime.now());
        return discountCouponRepository.save(coupon);
    }

    public double sumBalance() {
        return 0;
    }

    public double averageBalance() {
        return 0;
    }

    public Object getRecentTransactions(int i) {
        return null;
    }
}