package org.example._citizencard3.mapper;

import org.example._citizencard3.dto.response.SeatStatusResponse;
import org.example._citizencard3.dto.response.SeatLockResponse;
import org.example._citizencard3.model.SeatLockRedis;
import org.example._citizencard3.model.SeatStatus;
import org.springframework.stereotype.Component;

@Component
public class SeatLockMapper {

  // Redis鎖定資訊轉換為回應DTO
  public SeatLockResponse toResponse(SeatLockRedis seatLock) {
    if (seatLock == null) {
      return null;
    }

    return SeatLockResponse.builder()
        .scheduleId(seatLock.getScheduleId())
        .seatNumber(seatLock.getSeatNumber())
        .lockTime(seatLock.getLockTime())
        .expireTime(seatLock.getExpireTime())
        .success(seatLock.isValid())
        .message(seatLock.isValid() ? "座位鎖定成功" : "座位鎖定失敗")
        .build();
  }

  // Redis鎖定資訊轉換為座位狀態DTO
  public SeatStatusResponse toStatusResponse(SeatLockRedis seatLock) {
    return SeatStatusResponse.builder()
        .seatNumber(seatLock.getSeatNumber())
        .isAvailable(!seatLock.isValid())  // 如果鎖定有效，則不可用
        .build();
  }
}