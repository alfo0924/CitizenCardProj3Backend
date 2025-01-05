package org.example._citizencard3.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatLockRedis {
  private Long userId;            // 鎖定用戶ID
  private Long scheduleId;        // 場次ID
  private String seatNumber;      // 座位號碼
  private LocalDateTime lockTime;     // 鎖定時間
  private LocalDateTime expireTime;   // 過期時間
  private SeatLockStatus status;      // 鎖定狀態

  // 鎖定狀態枚舉
  public enum SeatLockStatus {
    LOCKED("已鎖定"),
    RELEASED("已釋放");

    private final String description;

    SeatLockStatus(String description) {
      this.description = description;
    }
  }

  // 檢查鎖定是否有效
  public boolean isValid() {
    return status == SeatLockStatus.LOCKED &&
        expireTime.isAfter(LocalDateTime.now());
  }
}