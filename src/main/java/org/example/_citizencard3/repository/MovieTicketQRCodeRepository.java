package org.example._citizencard3.repository;

import org.example._citizencard3.model.MovieTicketQRCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieTicketQRCodeRepository extends JpaRepository<MovieTicketQRCode, Long> {

    // 根據票券ID查詢QR碼
    Optional<MovieTicketQRCode> findByTicketId(Long ticketId);

    // 根據QR碼數據查詢
    Optional<MovieTicketQRCode> findByQrCodeData(String qrCodeData);

    // 查詢指定用戶的所有有效QR碼
    @Query("SELECT q FROM MovieTicketQRCode q JOIN MovieTicket t ON q.ticketId = t.id " +
            "WHERE t.user = :userId AND q.validUntil > :now AND q.isUsed = false")
    List<MovieTicketQRCode> findValidQRCodesByUserId(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

    // 查詢已過期的QR碼
    List<MovieTicketQRCode> findByValidUntilBeforeAndIsUsedFalse(LocalDateTime dateTime);

    // 查詢指定時間範圍內的QR碼
    List<MovieTicketQRCode> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    // 查詢指定票券ID列表的QR碼
    List<MovieTicketQRCode> findByTicketIdIn(List<Long> ticketIds);

    // 檢查QR碼是否有效
    @Query("SELECT CASE WHEN COUNT(q) > 0 THEN true ELSE false END FROM MovieTicketQRCode q " +
            "WHERE q.qrCodeData = :qrCodeData AND q.validUntil > :now AND q.isUsed = false")
    boolean isQRCodeValid(@Param("qrCodeData") String qrCodeData, @Param("now") LocalDateTime now);

    // 更新QR碼使用狀態
    @Modifying
    @Transactional
    @Query("UPDATE MovieTicketQRCode q SET q.isUsed = true, q.usedAt = :usedAt, q.updatedAt = :updatedAt " +
            "WHERE q.id = :id AND q.isUsed = false")
    int updateQRCodeUsed(
            @Param("id") Long id,
            @Param("usedAt") LocalDateTime usedAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    // 統計指定時間範圍內的QR碼使用情況
    @Query("SELECT COUNT(q) FROM MovieTicketQRCode q " +
            "WHERE q.createdAt BETWEEN :startTime AND :endTime AND q.isUsed = true")
    long countUsedQRCodesInPeriod(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 刪除過期且未使用的QR碼
    @Modifying
    @Transactional
    void deleteByValidUntilBeforeAndIsUsedFalse(LocalDateTime dateTime);
}
