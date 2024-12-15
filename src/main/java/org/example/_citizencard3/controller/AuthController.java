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
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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

            // 檢查帳號是否存在
            if (!authService.existsByEmail(request.getEmail())) {
                throw new CustomException("此帳號不存在請註冊", HttpStatus.NOT_FOUND);
            }

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

            return ResponseEntity.ok(result);
        } catch (UsernameNotFoundException e) {
            log.error("Login failed: User not found - {}", request.getEmail());
            throw new CustomException("此帳號不存在請註冊", HttpStatus.NOT_FOUND);
        } catch (BadCredentialsException e) {
            log.error("Login failed: Incorrect password for user - {}", request.getEmail());
            throw new CustomException("帳號密碼錯誤", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            throw new CustomException("登入失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            log.info("Processing registration for user: {}", request.getEmail());

            // 檢查帳號是否已存在
            if (authService.existsByEmail(request.getEmail())) {
                throw new CustomException("此電子郵件已被註冊", HttpStatus.CONFLICT);
            }

            request.setEmail(request.getEmail().toLowerCase().trim());
            request.setName(request.getName().trim());
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
                authService.logout(token.substring(7));
            }
            Map<String, String> response = new HashMap<>();
            response.put("message", "登出成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new CustomException("登出失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(CustomException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("status", ex.getStatus().value());
        return ResponseEntity.status(ex.getStatus()).body(response);
    }
}
