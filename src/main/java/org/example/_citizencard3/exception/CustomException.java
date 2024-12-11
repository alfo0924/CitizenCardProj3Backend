package org.example._citizencard3.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {

    private final String message;
    private final HttpStatus status;
    private final String errorCode;

    // 基本建構子
    public CustomException(String message) {
        this(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 帶狀態碼的建構子
    public CustomException(String message, HttpStatus status) {
        this(message, status, "ERROR_" + status.value());
    }

    // 完整建構子
    public CustomException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.message = message;
        this.status = status;
        this.errorCode = errorCode;
    }

    // 業務相關異常
    public static CustomException businessError(String message) {
        return new CustomException(message, HttpStatus.BAD_REQUEST, "BUSINESS_ERROR");
    }

    // 資料驗證異常
    public static CustomException validationError(String message) {
        return new CustomException(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
    }

    // 資源不存在異常
    public static CustomException notFound(String message) {
        return new CustomException(message, HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    // 請求參數錯誤異常
    public static CustomException badRequest(String message) {
        return new CustomException(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }

    // 未授權異常
    public static CustomException unauthorized(String message) {
        return new CustomException(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }

    // 權限不足異常
    public static CustomException forbidden(String message) {
        return new CustomException(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    // 資源衝突異常
    public static CustomException conflict(String message) {
        return new CustomException(message, HttpStatus.CONFLICT, "CONFLICT");
    }

    // 伺服器內部錯誤
    public static CustomException internalServerError(String message) {
        return new CustomException(message, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR");
    }

    // 資料庫操作異常
    public static CustomException databaseError(String message) {
        return new CustomException(message, HttpStatus.INTERNAL_SERVER_ERROR, "DATABASE_ERROR");
    }

    // 外部服務調用異常
    public static CustomException serviceError(String message) {
        return new CustomException(message, HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_ERROR");
    }

    // 系統維護異常
    public static CustomException maintenance(String message) {
        return new CustomException(message, HttpStatus.SERVICE_UNAVAILABLE, "MAINTENANCE");
    }

    // 超時異常
    public static CustomException timeout(String message) {
        return new CustomException(message, HttpStatus.REQUEST_TIMEOUT, "TIMEOUT");
    }

    // 參數為空異常
    public static CustomException nullParameter(String paramName) {
        return new CustomException(
                String.format("Parameter '%s' cannot be null", paramName),
                HttpStatus.BAD_REQUEST,
                "NULL_PARAMETER"
        );
    }

    // 獲取完整錯誤信息
    public String getFullMessage() {
        return String.format("[%s] %s", this.errorCode, this.message);
    }
}
