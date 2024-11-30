package org.example._citizencard3.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatsResponse {
    private long totalUsers;
    private long activeUsers;
    private long newUsersToday;
    private long verifiedUsers;
    private double activeUserPercentage;
    private double verificationRate;
}