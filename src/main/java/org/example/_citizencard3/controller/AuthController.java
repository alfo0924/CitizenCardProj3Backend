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

            if (!authService.existsByEmail(request.getEmail())) {
                log.error("Login failed: User not found - {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "此帳號不存在請註冊", "status", HttpStatus.NOT_FOUND.value()));
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
                    .createdAt(response.getCreatedAt())
                    .updatedAt(response.getUpdatedAt())
                    .version(response.getVersion())
                    .build());

            if (response.getWallet() != null) {
                result.put("wallet", response.getWallet());
            }

            return ResponseEntity.ok(result);
        } catch (BadCredentialsException e) {
            log.error("Login failed: Incorrect password for user - {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "帳號密碼錯誤", "status", HttpStatus.UNAUTHORIZED.value()));
        } catch (CustomException e) {
            log.error("Login failed: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus())
                    .body(Map.of("message", e.getMessage(), "status", e.getStatus().value()));
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "登入失敗", "status", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            log.info("Processing registration for user: {}", request.getEmail());

            if (authService.existsByEmail(request.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "此電子郵件已被註冊", "status", HttpStatus.CONFLICT.value()));
            }

            request.setEmail(request.getEmail().toLowerCase().trim());
            request.setName(request.getName().trim());
            request.setCreatedAt(LocalDateTime.now());
            request.setUpdatedAt(LocalDateTime.now());
            request.setVersion(0);
            request.setActive(true);
            request.setEmailVerified(false);

            UserResponse response = authService.register(request);
            Map<String, Object> result = new HashMap<>();
            result.put("user", response);
            result.put("status", HttpStatus.CREATED.value());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (CustomException e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus())
                    .body(Map.of("message", e.getMessage(), "status", e.getStatus().value()));
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "註冊失敗", "status", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                authService.logout(token.substring(7));
            }
            return ResponseEntity.ok(Map.of("message", "登出成功", "status", HttpStatus.OK.value()));
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "登出失敗", "status", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "無效的認證令牌", "status", HttpStatus.UNAUTHORIZED.value()));
            }
            String jwtToken = token.substring(7);
            UserResponse response = authService.getProfile(jwtToken);
            return ResponseEntity.ok(Map.of("user", response, "status", HttpStatus.OK.value()));
        } catch (CustomException e) {
            log.error("Get profile failed: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus())
                    .body(Map.of("message", e.getMessage(), "status", e.getStatus().value()));
        } catch (Exception e) {
            log.error("Get profile failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "獲取用戶資料失敗", "status", HttpStatus.INTERNAL_SERVER_ERROR.value()));
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
