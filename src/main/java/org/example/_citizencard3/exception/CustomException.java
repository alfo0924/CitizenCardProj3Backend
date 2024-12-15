package org.example._citizencard3.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final String message;
    private final HttpStatus status;

    public CustomException(String message, HttpStatus status) {
        super(message);
        this.message = message;
        this.status = status;
    }

    // 用戶相關異常
    public static CustomException userNotFound(String email) {
        return new CustomException(
                "此帳號不存在請註冊: " + email,
                HttpStatus.NOT_FOUND
        );
    }

    public static CustomException userAlreadyExists(String email) {
        return new CustomException(
                "此電子郵件已被註冊: " + email,
                HttpStatus.CONFLICT
        );
    }

    public static CustomException invalidCredentials() {
        return new CustomException(
                "帳號密碼錯誤",
                HttpStatus.UNAUTHORIZED
        );
    }

    public static CustomException userInactive() {
        return new CustomException(
                "此帳戶已被停用",
                HttpStatus.FORBIDDEN
        );
    }

    // 錢包相關異常
    public static CustomException walletNotFound(Long userId) {
        return new CustomException(
                "找不到用戶錢包: " + userId,
                HttpStatus.NOT_FOUND
        );
    }

    public static CustomException insufficientBalance() {
        return new CustomException(
                "餘額不足",
                HttpStatus.BAD_REQUEST
        );
    }

    // 電影相關異常
    public static CustomException movieNotFound(Long movieId) {
        return new CustomException(
                "找不到電影: " + movieId,
                HttpStatus.NOT_FOUND
        );
    }

    public static CustomException movieNotActive() {
        return new CustomException(
                "電影已下架",
                HttpStatus.BAD_REQUEST
        );
    }

    // 場次相關異常
    public static CustomException scheduleNotFound(Long scheduleId) {
        return new CustomException(
                "找不到場次: " + scheduleId,
                HttpStatus.NOT_FOUND
        );
    }

    public static CustomException noAvailableSeats() {
        return new CustomException(
                "此場次已無座位",
                HttpStatus.BAD_REQUEST
        );
    }

    // 商店相關異常
    public static CustomException storeNotFound(Long storeId) {
        return new CustomException(
                "找不到商店: " + storeId,
                HttpStatus.NOT_FOUND
        );
    }

    public static CustomException storeInactive() {
        return new CustomException(
                "商店已停業",
                HttpStatus.BAD_REQUEST
        );
    }

    // 電影票相關異常
    public static CustomException ticketNotFound(Long ticketId) {
        return new CustomException(
                "找不到電影票: " + ticketId,
                HttpStatus.NOT_FOUND
        );
    }

    public static CustomException invalidTicketStatus() {
        return new CustomException(
                "電影票狀態無效",
                HttpStatus.BAD_REQUEST
        );
    }

    // 優惠券相關異常
    public static CustomException couponNotFound(Long couponId) {
        return new CustomException(
                "找不到優惠券: " + couponId,
                HttpStatus.NOT_FOUND
        );
    }

    public static CustomException couponExpired() {
        return new CustomException(
                "優惠券已過期",
                HttpStatus.BAD_REQUEST
        );
    }

    // QR碼相關異常
    public static CustomException qrCodeNotFound(Long qrCodeId) {
        return new CustomException(
                "找不到QR碼: " + qrCodeId,
                HttpStatus.NOT_FOUND
        );
    }

    public static CustomException qrCodeExpired() {
        return new CustomException(
                "QR碼已過期",
                HttpStatus.BAD_REQUEST
        );
    }

    public static CustomException qrCodeAlreadyUsed() {
        return new CustomException(
                "QR碼已使用",
                HttpStatus.BAD_REQUEST
        );
    }
}
