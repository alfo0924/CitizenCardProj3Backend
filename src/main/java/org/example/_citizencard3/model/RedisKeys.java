package org.example._citizencard3.model;

public class RedisKeys {
  private static final String SEAT_LOCK_PREFIX = "seat_lock";

  public static String getSeatLockKey(Long scheduleId, String seatNumber) {
    return String.format("%s:%d:seat:%s", SEAT_LOCK_PREFIX, scheduleId, seatNumber);
  }

  public static String getScheduleSeatsKey(Long scheduleId) {
    return String.format("%s:%d:seats", SEAT_LOCK_PREFIX, scheduleId);
  }
}