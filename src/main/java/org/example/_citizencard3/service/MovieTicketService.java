package org.example._citizencard3.service;

import jakarta.transaction.Transactional;
import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example._citizencard3.dto.response.MovieTicketResponse;
import org.example._citizencard3.mapper.MovieTicketMapper;
import org.example._citizencard3.model.Movie;
import org.example._citizencard3.model.MovieTicket;
import org.example._citizencard3.model.MovieTicket.TicketStatus;
import org.example._citizencard3.model.Schedule;
import org.example._citizencard3.model.User;
import org.example._citizencard3.repository.MovieRepository;
import org.example._citizencard3.repository.MovieTicketRepository;
import org.example._citizencard3.repository.ScheduleRepository;
import org.example._citizencard3.repository.UserRepository;
import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieTicketService {

  private final MovieTicketRepository movieTicketRepository;
  private final MovieRepository movieRepository;
  private final ScheduleRepository scheduleRepository;
  private final UserRepository userRepository;
  private final MovieTicketMapper movieTicketMapper;

  public MovieTicketResponse createTicket(Long userId, Long movieId, Long scheduleId, String seatNumber) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("找不到使用者"));

    Movie movie = movieRepository.findById(movieId)
        .orElseThrow(() -> new RuntimeException("找不到電影"));

    Schedule schedule = scheduleRepository.findById(scheduleId)
        .orElseThrow(() -> new RuntimeException("找不到場次"));

    validateScheduleTime(schedule);
    validateSeatAvailability(scheduleId, seatNumber);

    MovieTicket ticket = MovieTicket.builder()
        .user(user)
        .movie(movie)
        .schedule(schedule)
        .seatNumber(seatNumber)
        .status(MovieTicket.TicketStatus.VALID)
        .build();

    return movieTicketMapper.toResponse(movieTicketRepository.save(ticket));
  }

  public List<MovieTicketResponse> getUserTickets(Long userId) {
    return movieTicketRepository.findByUserIdWithDetails(userId)
        .stream()
        .map(movieTicketMapper::toResponse)
        .collect(Collectors.toList());
  }

  private void validateScheduleTime(Schedule schedule) {
    if (schedule.isExpired()) {
      throw new RuntimeException("該場次已過期");
    }
  }

  private void validateSeatAvailability(Long scheduleId, String seatNumber) {
    if (movieTicketRepository.isSeatBooked(scheduleId, seatNumber)) {
      throw new RuntimeException("該座位已被訂購");
    }
  }

  //QRCode取得電影票資訊
  public MovieTicket getTicketById(Long ticketId) {
    return movieTicketRepository.findByIdWithDetails(ticketId)
        .orElseThrow(() -> new RuntimeException("找不到電影票：" + ticketId));
  }

  //取消電影票訂票
  @Transactional
  public MovieTicketResponse cancelTicket(Long userId, Long ticketId) {
    // 1. 先找到電影票
    MovieTicket ticket = movieTicketRepository.findByIdWithDetails(ticketId)
        .orElseThrow(() -> new RuntimeException("找不到電影票：" + ticketId));

    // 2. 確認是否為該用戶的票
    if (!userId.equals(ticket.getUserId())) {
      throw new RuntimeException("無權限取消此票券");
    }

    // 3. 檢查票券狀態
    if (!ticket.isValid()) {
      throw new RuntimeException("票券狀態不允許取消");
    }

    // 4. 檢查放映時間
    if (ticket.getSchedule().getShowTime().isBefore(LocalDateTime.now())) {
      throw new RuntimeException("已過放映時間，無法取消");
    }

    // 5. 執行取消
    ticket.cancel();
    return movieTicketMapper.toResponse(movieTicketRepository.save(ticket));
  }
}