package org.example._citizencard3.repository;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.model.RedisKeys;
import org.example._citizencard3.model.SeatLockRedis;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class SeatLockRepository {
  private final RedisTemplate<String, Object> redisTemplate;
  private static final long LOCK_DURATION = 10; // 10分鐘鎖定時間

  // 創建座位鎖定
  public boolean lockSeat(Long scheduleId, String seatNumber, Long userId) {
    String key = RedisKeys.getSeatLockKey(scheduleId, seatNumber);
    LocalDateTime now = LocalDateTime.now();

    SeatLockRedis lockInfo = SeatLockRedis.builder()
        .userId(userId)
        .scheduleId(scheduleId)
        .seatNumber(seatNumber)
        .lockTime(now)
        .expireTime(now.plusMinutes(LOCK_DURATION))
        .status(SeatLockRedis.SeatLockStatus.LOCKED)
        .build();

    // 使用 setIfAbsent 確保原子性操作
    Boolean success = redisTemplate.opsForValue()
        .setIfAbsent(key, lockInfo, LOCK_DURATION, TimeUnit.MINUTES);

    return Boolean.TRUE.equals(success);
  }

  // 釋放座位鎖定
  public boolean releaseLock(Long scheduleId, String seatNumber) {
    String key = RedisKeys.getSeatLockKey(scheduleId, seatNumber);
    return Boolean.TRUE.equals(redisTemplate.delete(key));
  }

  // 檢查座位是否被鎖定
  public boolean isLocked(Long scheduleId, String seatNumber) {
    String key = RedisKeys.getSeatLockKey(scheduleId, seatNumber);
    SeatLockRedis lockInfo = (SeatLockRedis) redisTemplate.opsForValue().get(key);
    return lockInfo != null && lockInfo.isValid();
  }

  // 獲取座位鎖定信息
  public SeatLockRedis getLockInfo(Long scheduleId, String seatNumber) {
    String key = RedisKeys.getSeatLockKey(scheduleId, seatNumber);
    return (SeatLockRedis) redisTemplate.opsForValue().get(key);
  }

  // 檢查是否是指定用戶鎖定的座位
  public boolean isLockedByUser(Long scheduleId, String seatNumber, Long userId) {
    SeatLockRedis lockInfo = getLockInfo(scheduleId, seatNumber);
    return lockInfo != null &&
        lockInfo.isValid() &&
        lockInfo.getUserId().equals(userId);
  }

  // 更新鎖定時間
  public boolean renewLock(Long scheduleId, String seatNumber, Long userId) {
    if (!isLockedByUser(scheduleId, seatNumber, userId)) {
      return false;
    }

    SeatLockRedis lockInfo = getLockInfo(scheduleId, seatNumber);
    if (lockInfo != null) {
      lockInfo.setExpireTime(LocalDateTime.now().plusMinutes(LOCK_DURATION));
      String key = RedisKeys.getSeatLockKey(scheduleId, seatNumber);
      redisTemplate.opsForValue().set(key, lockInfo, LOCK_DURATION, TimeUnit.MINUTES);
      return true;
    }
    return false;
  }
}