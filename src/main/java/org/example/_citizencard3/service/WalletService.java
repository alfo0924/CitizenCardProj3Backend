package org.example._citizencard3.service;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.dto.response.MovieTicketResponse;
import org.example._citizencard3.dto.response.DiscountCouponResponse;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.model.MovieTicket;
import org.example._citizencard3.model.DiscountCoupon;
import org.example._citizencard3.model.User;
import org.example._citizencard3.repository.MovieTicketRepository;
import org.example._citizencard3.repository.DiscountCouponRepository;
import org.example._citizencard3.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserRepository userRepository;
    private final MovieTicketRepository movieTicketRepository;
    private final DiscountCouponRepository discountCouponRepository;

    // 獲取用戶票券資料
    public Map<String, Object> getTickets(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("用戶不存在", HttpStatus.NOT_FOUND));

        List<MovieTicketResponse> movieTickets = movieTicketRepository.findByUserId(userId)
                .stream()
                .map(this::convertToMovieTicketResponse)
                .collect(Collectors.toList());

        List<DiscountCouponResponse> discountCoupons = discountCouponRepository.findByUserId(userId)
                .stream()
                .map(this::convertToDiscountCouponResponse)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("movieTickets", movieTickets);
        result.put("discountCoupons", discountCoupons);
        return result;
    }

    // 使用優惠券
    @Transactional
    public void useCoupon(Long userId, Long couponId) {
        DiscountCoupon coupon = discountCouponRepository.findById(couponId)
                .orElseThrow(() -> new CustomException("優惠券不存在", HttpStatus.NOT_FOUND));

        if (!coupon.getUser().getId().equals(userId)) {
            throw new CustomException("無權使用此優惠券", HttpStatus.FORBIDDEN);
        }

        if (!coupon.isValid()) {
            throw new CustomException("優惠券已失效或過期", HttpStatus.BAD_REQUEST);
        }

        coupon.setStatus(DiscountCoupon.CouponStatus.USED);
        discountCouponRepository.save(coupon);
    }

    // 檢查票券狀態
    @Transactional
    public void checkTicketStatus(Long userId, Long ticketId) {
        MovieTicket ticket = movieTicketRepository.findById(ticketId)
                .orElseThrow(() -> new CustomException("票券不存在", HttpStatus.NOT_FOUND));

        if (!ticket.getUser().getId().equals(userId)) {
            throw new CustomException("無權查看此票券", HttpStatus.FORBIDDEN);
        }

        // 檢查票券是否過期
        if (ticket.getSchedule().getShowTime().isBefore(LocalDateTime.now())) {
            ticket.setStatus(MovieTicket.TicketStatus.EXPIRED);
            movieTicketRepository.save(ticket);
        }
    }

    // 轉換為電影票響應對象
    private MovieTicketResponse convertToMovieTicketResponse(MovieTicket ticket) {
        return MovieTicketResponse.builder()
                .id(ticket.getId())
                .movieTitle(ticket.getMovie().getTitle())
                .showTime(ticket.getSchedule().getShowTime())
                .hall(ticket.getSchedule().getHall())
                .seatNumber(ticket.getSeatNumber())
                .status(ticket.getStatus().name())
                .build();
    }

    // 轉換為優惠券響應對象
    private DiscountCouponResponse convertToDiscountCouponResponse(DiscountCoupon coupon) {
        return DiscountCouponResponse.builder()
                .id(coupon.getId())
                .title(coupon.getTitle())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType().name())
                .discountValue(coupon.getDiscountValue())
                .status(coupon.getStatus().name())
                .expiryDate(coupon.getExpiryDate())
                .build();
    }
}