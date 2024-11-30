package org.example._citizencard3.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MovieRequest {
    @NotBlank(message = "電影標題不能為空")
    private String title;

    @NotBlank(message = "導演不能為空")
    private String director;

    @NotBlank(message = "演員不能為空")
    private String cast;

    @NotNull(message = "片長不能為空")
    private Integer duration;

    @NotBlank(message = "類型不能為空")
    private String genre;

    @NotBlank(message = "分級不能為空")
    private String rating;

    private String posterUrl;

    private String trailerUrl;

    private String description;  // 添加描述欄位

    @NotNull(message = "上映日期不能為空")
    private LocalDateTime releaseDate;

    @NotNull(message = "下檔日期不能為空")
    private LocalDateTime endDate;

    @NotNull(message = "是否上映不能為空")
    private Boolean isShowing;

    @NotNull(message = "票價不能為空")
    private Integer price;
}