package org.example._citizencard3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatLockResponse {
  private Long scheduleId;          // 場次ID
  private String seatNumber;        // 座位號碼
  private LocalDateTime lockTime;    // 鎖定時間
  private LocalDateTime expireTime;  // 過期時間
  private boolean success;          // 鎖定是否成功
  private String message;           // 回應訊息
}