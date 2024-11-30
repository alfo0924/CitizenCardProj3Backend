package org.example._citizencard3.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StoreRequest {
    @NotBlank(message = "商店名稱不能為空")
    private String name;

    @NotBlank(message = "商店類別不能為空")
    private String category;

    private String description;

    @NotBlank(message = "地址不能為空")
    private String address;

    private String phone;

    private String email;

    private String website;

    private String openingHours;

    private String imageUrl;

    private String discountInfo;

    private Double latitude;

    private Double longitude;
}