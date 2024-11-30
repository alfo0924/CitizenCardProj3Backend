package org.example._citizencard3.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoreResponse {
    private Long id;
    private String name;
    private String category;
    private String description;
    private String address;
    private String phone;
    private String email;
    private String website;
    private String openingHours;
    private String imageUrl;
    private String discountInfo;
    private Double latitude;
    private Double longitude;
    private Double rating;
    private Integer ratingCount;
}