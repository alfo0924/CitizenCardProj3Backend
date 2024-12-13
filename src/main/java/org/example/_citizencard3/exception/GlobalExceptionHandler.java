package org.example._citizencard3.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ?
                                fieldError.getDefaultMessage() : "驗證錯誤",
                        (error1, error2) -> error1
                ));

        response.put("message", "請求參數驗證失敗");
        response.put("errors", errors);
        response.put("status", HttpStatus.BAD_REQUEST.value());

        log.error("參數驗證失敗: {}", errors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(CustomException ex) {
        log.error("業務異常: {}", ex.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("status", ex.getStatus().value());
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            DisabledException.class,
            LockedException.class,
            AuthenticationException.class
    })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(Exception ex) {
        String message = "認證失敗";
        if (ex instanceof BadCredentialsException) {
            message = "帳號或密碼錯誤";
        } else if (ex instanceof DisabledException) {
            message = "帳號已被停用";
        } else if (ex instanceof LockedException) {
            message = "帳號已被鎖定";
        }

        log.error("認證異常: {}", ex.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("權限不足: {}", ex.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "您沒有權限執行此操作");
        response.put("status", HttpStatus.FORBIDDEN.value());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleEntityNotFoundException(EntityNotFoundException ex) {
        log.error("實體未找到: {}", ex.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            DataIntegrityViolationException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleDataValidationException(Exception ex) {
        log.error("數據驗證失敗: {}", ex.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "數據驗證失敗");
        response.put("detail", ex.getMessage());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.error("資源未找到: {}", ex.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "請求的資源不存在");
        response.put("status", HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleMissingRequestHeaderException(
            MissingRequestHeaderException ex) {
        log.error("缺少請求標頭: {}", ex.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "缺少必要的請求標頭: " + ex.getHeaderName());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleAllUncaughtException(Exception ex) {
        log.error("系統異常: ", ex);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "系統發生錯誤，請稍後再試");
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

        // 在開發環境中可以添加更多詳細信息
        if (log.isDebugEnabled()) {
            response.put("error", ex.getClass().getName());
            response.put("detail", ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
