package org.example._citizencard3.dto.request;


import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class SeatLockRequest {
  @NotNull(message = "場次ID不能為空")
  private Long scheduleId;

  @NotNull(message = "座位號碼不能為空")
  private String seatNumber;
}