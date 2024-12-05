package org.example._citizencard3.service;

import org.example._citizencard3.dto.request.LoginRequest;
import org.example._citizencard3.dto.request.RegisterRequest;
import org.example._citizencard3.dto.response.LoginResponse;
import org.example._citizencard3.dto.response.UserResponse;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.model.User;
import org.example._citizencard3.model.Wallet;
import org.example._citizencard3.repository.UserRepository;
import org.example._citizencard3.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new CustomException("用戶不存在", HttpStatus.NOT_FOUND));

            String token = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            // 更新最後登入時間
            user.setLastLoginTime(LocalDateTime.now());
            userRepository.save(user);

            return LoginResponse.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getExpirationTime())
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole().name())
                    .avatar(user.getAvatar())
                    .isEmailVerified(user.isEmailVerified())
                    .lastLoginTime(user.getLastLoginTime().toString())
                    .build();
        } catch (Exception e) {
            throw new CustomException("帳號或密碼錯誤", HttpStatus.UNAUTHORIZED);
        }
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("此電子郵件已被註冊", HttpStatus.CONFLICT);
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new CustomException("密碼不一致", HttpStatus.BAD_REQUEST);
        }

        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .birthday(request.getBirthday())
                .gender(request.getGender())
                .address(request.getAddress())
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(0.0)
                .createdAt(now)
                .updatedAt(now)
                .build();
        user.setWallet(wallet);

        user = userRepository.save(user);

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .birthday(user.getBirthday())
                .gender(user.getGender())
                .address(user.getAddress())
                .role(user.getRole().name())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String tokenValue = token.substring(7);
            jwtTokenProvider.invalidateToken(tokenValue);
        }
    }

    public UserResponse getProfile(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String tokenValue = token.substring(7);
            String email = jwtTokenProvider.getEmailFromToken(tokenValue);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomException("用戶不存在", HttpStatus.NOT_FOUND));

            return UserResponse.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .birthday(user.getBirthday())
                    .gender(user.getGender())
                    .address(user.getAddress())
                    .role(user.getRole().name())
                    .active(user.isActive())
                    .createdAt(user.getCreatedAt())
                    .build();
        }
        throw new CustomException("無效的認證令牌", HttpStatus.UNAUTHORIZED);
    }

    public boolean validateToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return jwtTokenProvider.validateToken(token.substring(7));
        }
        return false;
    }

    public LoginResponse refreshToken(String token, String refreshToken) {
        // Remove refresh token validation since it's not implemented
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("用戶不存在", HttpStatus.NOT_FOUND));

        // Generate new tokens
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                user.getAuthorities()
        );

        String newToken = jwtTokenProvider.generateToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        return LoginResponse.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }

    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
    public void sendPasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        "找不到使用者",
                        HttpStatus.NOT_FOUND
                ));

        String resetToken = jwtTokenProvider.generatePasswordResetToken(email);
        // TODO: Implement email sending logic
        // For example: emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }

    public void resetPassword(String token, String newPassword) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new CustomException(
                    "無效的重置令牌",
                    HttpStatus.BAD_REQUEST
            );
        }

        String email = jwtTokenProvider.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        "找不到使用者",
                        HttpStatus.NOT_FOUND
                ));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}