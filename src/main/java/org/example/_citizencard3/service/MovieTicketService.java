package org.example._citizencard3.service;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.dto.response.MovieTicketResponse;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.model.MovieTicket;
import org.example._citizencard3.model.User;
import org.example._citizencard3.repository.MovieTicketRepository;
import org.example._citizencard3.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieTicketService {

    private final MovieTicketRepository movieTicketRepository;
    private final UserRepository userRepository;

    // 獲取當前用戶的電影票
    public List<MovieTicketResponse> getCurrentUserTickets() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("用戶不存在", HttpStatus.NOT_FOUND));

        return movieTicketRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::convertToMovieTicketResponse)
                .collect(Collectors.toList());
    }

    // 獲取電影票詳情
    public MovieTicketResponse getTicketById(Long ticketId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("用戶不存在", HttpStatus.NOT_FOUND));

        MovieTicket ticket = movieTicketRepository.findById(ticketId)
                .orElseThrow(() -> new CustomException("電影票不存在", HttpStatus.NOT_FOUND));

        if (!ticket.getUser().getId().equals(user.getId())) {
            throw new CustomException("無權查看此電影票", HttpStatus.FORBIDDEN);
        }

        return convertToMovieTicketResponse(ticket);
    }

    // 檢查電影票狀態
    @Transactional
    public void checkTicketStatus(Long ticketId) {
        MovieTicket ticket = movieTicketRepository.findById(ticketId)
                .orElseThrow(() -> new CustomException("電影票不存在", HttpStatus.NOT_FOUND));

        if (ticket.getSchedule().getShowTime().isBefore(LocalDateTime.now())) {
            ticket.setStatus(MovieTicket.TicketStatus.EXPIRED);
            movieTicketRepository.save(ticket);
        }
    }

    // 取消電影票
    @Transactional
    public void cancelTicket(Long ticketId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("用戶不存在", HttpStatus.NOT_FOUND));

        MovieTicket ticket = movieTicketRepository.findById(ticketId)
                .orElseThrow(() -> new CustomException("電影票不存在", HttpStatus.NOT_FOUND));

        if (!ticket.getUser().getId().equals(user.getId())) {
            throw new CustomException("無權取消此電影票", HttpStatus.FORBIDDEN);
        }

        if (ticket.getStatus() != MovieTicket.TicketStatus.VALID) {
            throw new CustomException("此電影票無法取消", HttpStatus.BAD_REQUEST);
        }

        ticket.setStatus(MovieTicket.TicketStatus.CANCELLED);
        movieTicketRepository.save(ticket);
    }

    // 轉換為響應對象
    private MovieTicketResponse convertToMovieTicketResponse(MovieTicket ticket) {
        return MovieTicketResponse.builder()
                .id(ticket.getId())
                .movieTitle(ticket.getMovie().getTitle())
                .showTime(ticket.getSchedule().getShowTime())
                .hall(ticket.getSchedule().getHall())
                .seatNumber(ticket.getSeatNumber())
                .status(ticket.getStatus().name())
                .price(Double.valueOf(ticket.getMovie().getPrice()))
                .posterUrl(ticket.getMovie().getPosterUrl())
                .director(ticket.getMovie().getDirector())
                .cast(ticket.getMovie().getCast())
                .duration(ticket.getMovie().getDuration())
                .genre(ticket.getMovie().getGenre())
                .rating(ticket.getMovie().getRating())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}