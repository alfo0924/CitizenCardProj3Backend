package org.example._citizencard3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example._citizencard3.model.SeatStatus;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatStatusResponse {
  private String seatNumber;     // 座位號碼
  private boolean isAvailable;   // 是否可選擇
}