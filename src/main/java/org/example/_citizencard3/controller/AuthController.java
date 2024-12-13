package org.example._citizencard3.controller;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.dto.request.LoginRequest;
import org.example._citizencard3.dto.request.RegisterRequest;
import org.example._citizencard3.dto.response.LoginResponse;
import org.example._citizencard3.dto.response.UserResponse;
import org.example._citizencard3.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "http://localhost:3009")
                .header("Access-Control-Allow-Credentials", "true")
                .body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            UserResponse response = authService.register(request);
            return ResponseEntity.ok()
                    .header("Access-Control-Allow-Origin", "http://localhost:3009")
                    .header("Access-Control-Allow-Credentials", "true")
                    .body(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest()
                    .header("Access-Control-Allow-Origin", "http://localhost:3009")
                    .header("Access-Control-Allow-Credentials", "true")
                    .body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        authService.logout(token);
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "http://localhost:3009")
                .header("Access-Control-Allow-Credentials", "true")
                .build();
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        UserResponse response = authService.getProfile(token);
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "http://localhost:3009")
                .header("Access-Control-Allow-Credentials", "true")
                .body(response);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkToken(
            @RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.ok()
                    .header("Access-Control-Allow-Origin", "http://localhost:3009")
                    .header("Access-Control-Allow-Credentials", "true")
                    .body(false);
        }
        token = token.substring(7);
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "http://localhost:3009")
                .header("Access-Control-Allow-Credentials", "true")
                .body(isValid);
    }

    @PostMapping("/validate-email")
    public ResponseEntity<Boolean> validateEmail(@RequestParam String email) {
        boolean isAvailable = authService.isEmailAvailable(email);
        return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "http://localhost:3009")
                .header("Access-Control-Allow-Credentials", "true")
                .body(isAvailable);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.badRequest()
                .header("Access-Control-Allow-Origin", "http://localhost:3009")
                .header("Access-Control-Allow-Credentials", "true")
                .body(errorResponse);
    }
}
