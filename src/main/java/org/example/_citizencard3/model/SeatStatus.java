package org.example._citizencard3.model;

public enum SeatStatus {
  AVAILABLE("可選擇"),
  LOCKED("已鎖定"),
  BOOKED("已訂位");

  private final String description;

  SeatStatus(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}