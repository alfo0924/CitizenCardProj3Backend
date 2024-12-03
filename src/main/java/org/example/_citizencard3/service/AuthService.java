package org.example._citizencard3.service;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.dto.request.LoginRequest;
import org.example._citizencard3.dto.request.RegisterRequest;
import org.example._citizencard3.dto.response.LoginResponse;
import org.example._citizencard3.dto.response.UserResponse;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.model.User;
import org.example._citizencard3.model.Wallet;

import org.example._citizencard3.repository.UserRepository;
import org.example._citizencard3.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

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

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .birthday(request.getBirthday())
                .gender(request.getGender())
                .address(request.getAddress())
                .active(true)
                .build();

        // 創建錢包
        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(0.0)
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
        String tokenValue = token.substring(7);
        jwtTokenProvider.invalidateToken(tokenValue);
    }

    public UserResponse getProfile(String token) {
        String email = jwtTokenProvider.getEmailFromToken(token.substring(7));
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

    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token.substring(7));
    }

    public LoginResponse refreshToken(String token, String refreshToken) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new CustomException("無效的刷新令牌", HttpStatus.UNAUTHORIZED);
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("用戶不存在", HttpStatus.NOT_FOUND));

        String newToken = jwtTokenProvider.generateToken(email);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);

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
    /**
     * 檢查郵箱是否可用
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * 發送密碼重置郵件
     */
    public void sendPasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        "找不到使用者",
                        HttpStatus.NOT_FOUND
                ));

        String resetToken = jwtTokenProvider.generatePasswordResetToken(email);
        // TODO: 實現郵件發送邏輯
        // emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }

    /**
     * 重置密碼
     */
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