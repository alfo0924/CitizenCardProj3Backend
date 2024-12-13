package org.example._citizencard3.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizencard3.dto.request.LoginRequest;
import org.example._citizencard3.dto.request.RegisterRequest;
import org.example._citizencard3.dto.response.LoginResponse;
import org.example._citizencard3.dto.response.UserResponse;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
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
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.error("Login failed for user: {}", request.getEmail());
            throw new CustomException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            UserResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (CustomException e) {
            log.error("Registration failed: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                authService.logout(jwtToken);
            }
            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully logged out");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            throw new CustomException("Logout failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                throw new CustomException("Invalid token format", HttpStatus.UNAUTHORIZED);
            }
            String jwtToken = token.substring(7);
            UserResponse response = authService.getProfile(jwtToken);
            return ResponseEntity.ok(response);
        } catch (CustomException e) {
            log.error("Failed to get profile: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkToken(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                response.put("valid", false);
                return ResponseEntity.ok(response);
            }
            String jwtToken = token.substring(7);
            boolean isValid = authService.validateToken(jwtToken);
            response.put("valid", isValid);
            if (isValid) {
                response.put("user", authService.getProfile(jwtToken));
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            response.put("valid", false);
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/validate-email")
    public ResponseEntity<Map<String, Object>> validateEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isAvailable = authService.isEmailAvailable(email);
            response.put("available", isAvailable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Email validation failed: {}", e.getMessage());
            throw new CustomException("Email validation failed", HttpStatus.INTERNAL_SERVER_ERROR);
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
        response.put("message", "An unexpected error occurred");
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
