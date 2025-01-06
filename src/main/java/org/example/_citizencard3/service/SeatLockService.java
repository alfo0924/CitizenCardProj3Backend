package org.example._citizencard3.service;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.dto.request.SeatLockRequest;
import org.example._citizencard3.dto.response.SeatLockResponse;
import org.example._citizencard3.dto.response.SeatStatusResponse;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.mapper.SeatLockMapper;
import org.example._citizencard3.model.SeatLockRedis;
import org.example._citizencard3.model.SeatStatus;
import org.example._citizencard3.repository.MovieTicketRepository;
import org.example._citizencard3.repository.SeatLockRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatLockService {
  private final SeatLockRepository seatLockRepository;
  private final MovieTicketRepository movieTicketRepository;
  private final SeatLockMapper seatLockMapper;

  /**
   * 鎖定座位
   */
  public SeatLockResponse lockSeat(SeatLockRequest request, Long userId) {
    // 檢查座位是否已被訂購
    if (movieTicketRepository.isSeatBooked(request.getScheduleId(), request.getSeatNumber())) {
      throw new CustomException("座位已被訂購", HttpStatus.BAD_REQUEST);
    }

    // 檢查座位是否已被鎖定
    if (seatLockRepository.isLocked(request.getScheduleId(), request.getSeatNumber())) {
      throw new CustomException("座位已被鎖定", HttpStatus.BAD_REQUEST);
    }

    // 嘗試鎖定座位
    boolean success = seatLockRepository.lockSeat(
        request.getScheduleId(),
        request.getSeatNumber(),
        userId
    );

    if (!success) {
      throw new CustomException("座位鎖定失敗", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 獲取鎖定資訊並返回
    SeatLockRedis lockInfo = seatLockRepository.getLockInfo(
        request.getScheduleId(),
        request.getSeatNumber()
    );

    return seatLockMapper.toResponse(lockInfo);
  }

  /**
   * 釋放座位鎖定
   */
  public boolean releaseLock(Long scheduleId, String seatNumber, Long userId) {
    // 檢查是否是該用戶鎖定的座位
    if (!seatLockRepository.isLockedByUser(scheduleId, seatNumber, userId)) {
      throw new CustomException("無權釋放此座位鎖定", HttpStatus.FORBIDDEN);
    }

    return seatLockRepository.releaseLock(scheduleId, seatNumber);
  }

  /**
   * 獲取座位狀態
   */
  public SeatStatusResponse getSeatStatus(Long scheduleId, String seatNumber) {
    // 檢查座位是否已被訂購
    if (movieTicketRepository.isSeatBooked(scheduleId, seatNumber)) {
      return SeatStatusResponse.builder()
          .seatNumber(seatNumber)
          .isAvailable(false)
          .build();
    }

    // 檢查座位鎖定狀態
    SeatLockRedis lockInfo = seatLockRepository.getLockInfo(scheduleId, seatNumber);
    if (lockInfo != null && lockInfo.isValid()) {
      return SeatStatusResponse.builder()
          .seatNumber(seatNumber)
          .isAvailable(false)
          .build();
    }

    // 座位可用
    return SeatStatusResponse.builder()
        .seatNumber(seatNumber)
        .isAvailable(true)
        .build();
  }

  /**
   * 更新座位鎖定時間
   */
  public SeatLockResponse renewLock(Long scheduleId, String seatNumber, Long userId) {
    boolean success = seatLockRepository.renewLock(scheduleId, seatNumber, userId);
    if (!success) {
      throw new CustomException("更新座位鎖定失敗", HttpStatus.BAD_REQUEST);
    }

    SeatLockRedis lockInfo = seatLockRepository.getLockInfo(scheduleId, seatNumber);
    return seatLockMapper.toResponse(lockInfo);
  }

  /**
   * 獲取場次所有座位狀態
   */
  public List<SeatStatusResponse> getAllSeatsStatus(Long scheduleId) {
    List<SeatStatusResponse> statusList = new ArrayList<>();

    // 獲取已訂購的座位
    List<String> bookedSeats = movieTicketRepository.findBookedSeatsByScheduleId(scheduleId);

    // A-J排，每排1-20號
    for (char row = 'A'; row <= 'J'; row++) {
      for (int num = 1; num <= 20; num++) {
        String seatNumber = row + String.valueOf(num);
        // 檢查座位是否被鎖定（是否有人正在選購）
        SeatLockRedis lockInfo = seatLockRepository.getLockInfo(scheduleId, seatNumber);
        boolean isAvailable = lockInfo == null || !lockInfo.isValid();

        statusList.add(SeatStatusResponse.builder()
            .seatNumber(seatNumber)
            .isAvailable(isAvailable)  // 如果沒有被鎖定，代表可以選擇
            .build());
      }
    }

    return statusList;
  }

  /**
   * 檢查座位是否被指定用戶鎖定
   */
  public boolean isLockedByUser(Long scheduleId, String seatNumber, Long userId) {
    return seatLockRepository.isLockedByUser(scheduleId, seatNumber, userId);
  }
}