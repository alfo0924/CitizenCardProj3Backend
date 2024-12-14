package org.example._citizencard3.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizencard3.dto.request.LoginRequest;
import org.example._citizencard3.dto.request.RegisterRequest;
import org.example._citizencard3.dto.response.LoginResponse;
import org.example._citizencard3.dto.response.UserResponse;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3009", allowCredentials = "true")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Attempting login for user: {}", request.getEmail());
            LoginResponse response = authService.login(request);

            Map<String, Object> result = new HashMap<>();
            result.put("token", response.getToken());
            result.put("user", UserResponse.builder()
                    .id(response.getId())
                    .name(response.getName())
                    .email(response.getEmail())
                    .phone(response.getPhone())
                    .birthday(response.getBirthday())
                    .gender(response.getGender())
                    .role(response.getRole())
                    .address(response.getAddress())
                    .avatar(response.getAvatar())
                    .active(response.isActive())
                    .emailVerified(response.isEmailVerified())
                    .lastLoginTime(LocalDateTime.now())
                    .lastLoginIp(request.getIpAddress())
                    .build());

            if (response.getWallet() != null) {
                result.put("wallet", response.getWallet());
            }

            log.info("Login successful for user: {}", request.getEmail());
            return ResponseEntity.ok(result);
        } catch (UsernameNotFoundException e) {
            log.error("Login failed: User not found - {}", request.getEmail());
            throw new CustomException("無此帳號，請先註冊", HttpStatus.NOT_FOUND);
        } catch (BadCredentialsException e) {
            log.error("Login failed: Incorrect password for user - {}", request.getEmail());
            throw new CustomException("密碼錯誤", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            throw new CustomException("登入失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            log.info("Processing registration for user: {}", request.getEmail());
            validateRegistrationRequest(request);

            request.setEmail(request.getEmail().toLowerCase().trim());
            request.setName(request.getName().trim());
            request.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
            request.setGender(request.getGender() != null ? request.getGender().trim() : null);
            request.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
            request.setCreatedAt(LocalDateTime.now());
            request.setUpdatedAt(LocalDateTime.now());
            request.setVersion(0);
            request.setActive(true);
            request.setEmailVerified(false);

            UserResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (CustomException e) {
            log.error("Registration failed: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                authService.logout(jwtToken);
            }
            Map<String, String> response = new HashMap<>();
            response.put("message", "登出成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new CustomException("登出失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(
            @RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new CustomException("無效的認證令牌", HttpStatus.UNAUTHORIZED);
        }
        String jwtToken = token.substring(7);
        UserResponse response = authService.getProfile(jwtToken);
        return ResponseEntity.ok(response);
    }

    private void validateRegistrationRequest(RegisterRequest request) {
        if (request.getName() == null || request.getName().trim().length() < 2 ||
                request.getName().trim().length() > 50) {
            throw new CustomException("姓名長度必須在2-50個字元之間", HttpStatus.BAD_REQUEST);
        }
        if (request.getEmail() == null || !request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$") ||
                request.getEmail().length() > 100) {
            throw new CustomException("無效的電子郵件格式", HttpStatus.BAD_REQUEST);
        }
        if (request.getPassword() == null || request.getPassword().length() < 8 ||
                request.getPassword().length() > 255) {
            throw new CustomException("密碼長度必須在8-255個字元之間", HttpStatus.BAD_REQUEST);
        }
        if (request.getPhone() != null && !request.getPhone().matches("^09\\d{8}$")) {
            throw new CustomException("無效的手機號碼格式", HttpStatus.BAD_REQUEST);
        }
        if (request.getBirthday() != null && !request.getBirthday().matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new CustomException("無效的生日格式", HttpStatus.BAD_REQUEST);
        }
        if (request.getGender() != null && !request.getGender().matches("^(MALE|FEMALE)$")) {
            throw new CustomException("無效的性別格式", HttpStatus.BAD_REQUEST);
        }
        if (request.getAddress() != null && request.getAddress().length() > 500) {
            throw new CustomException("地址長度不能超過500個字元", HttpStatus.BAD_REQUEST);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        response.put("errors", errors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(CustomException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("status", ex.getStatus().value());
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        log.error("Unexpected error occurred: ", ex);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "系統發生未預期的錯誤");
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
