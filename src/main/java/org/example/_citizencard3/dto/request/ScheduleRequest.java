package org.example._citizencard3.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {

    @NotNull(message = "電影ID不能為空")
    private Long movieId;

    @NotNull(message = "放映時間不能為空")
    private LocalDateTime showTime;

    @NotNull(message = "影廳不能為空")
    private String hall;

    @NotNull(message = "總座位數不能為空")
    private Integer totalSeats;

    @NotNull(message = "可用座位數不能為空")
    private Integer availableSeats;

    private Boolean active = true;
}
