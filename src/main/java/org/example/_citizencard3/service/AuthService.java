package org.example._citizencard3.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizencard3.dto.request.LoginRequest;
import org.example._citizencard3.dto.request.RegisterRequest;
import org.example._citizencard3.dto.response.LoginResponse;
import org.example._citizencard3.dto.response.UserResponse;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.model.User;
import org.example._citizencard3.model.Wallet;
import org.example._citizencard3.repository.UserRepository;
import org.example._citizencard3.repository.WalletRepository;
import org.example._citizencard3.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^09\\d{8}$");

    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            validateLoginRequest(request);

            // 檢查帳號是否存在
            if (!userRepository.existsByEmail(request.getEmail())) {
                throw new CustomException("此帳號不存在請註冊", HttpStatus.NOT_FOUND);
            }

            User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                    .orElseThrow(() -> new CustomException("用戶不存在", HttpStatus.NOT_FOUND));

            if (!user.isActive()) {
                throw new CustomException("帳戶已被停用", HttpStatus.FORBIDDEN);
            }

            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
                );
                String token = jwtTokenProvider.generateToken(authentication);

                // 更新登入資訊
                user.setLastLoginTime(LocalDateTime.now());
                user.setLastLoginIp(request.getIpAddress());
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);

                return buildLoginResponse(user, token);
            } catch (BadCredentialsException e) {
                throw new CustomException("帳號密碼錯誤", HttpStatus.UNAUTHORIZED);
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("登入失敗", e);
            throw new CustomException("登入失敗，請稍後再試", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public UserResponse register(@Valid RegisterRequest request) {
        try {
            validateRegistrationRequest(request);

            if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
                throw new CustomException("此電子郵件已被註冊", HttpStatus.CONFLICT);
            }

            LocalDateTime now = LocalDateTime.now();
            User user = new User();
            user.setName(request.getName().trim());
            user.setEmail(request.getEmail().toLowerCase().trim());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setPhone(request.getPhone());
            user.setBirthday(request.getBirthday());
            user.setGender(request.getGender());
            user.setRole("ROLE_USER");
            user.setActive(true);
            user.setEmailVerified(false);
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            user.setVersion(0);

            user = userRepository.save(user);

            // 創建錢包
            Wallet wallet = new Wallet();
            wallet.setUser(user);
            wallet.setBalance(0.0);
            wallet.setCreatedAt(now);
            wallet.setUpdatedAt(now);
            walletRepository.save(wallet);

            return buildUserResponse(user);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("註冊失敗", e);
            throw new CustomException("註冊失敗，請稍後再試", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateLoginRequest(LoginRequest request) {
        if (!StringUtils.hasText(request.getEmail()) || !EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            throw new CustomException("請輸入有效的電子郵件", HttpStatus.BAD_REQUEST);
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new CustomException("請輸入密碼", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateRegistrationRequest(RegisterRequest request) {
        if (!StringUtils.hasText(request.getName()) || request.getName().length() > 50) {
            throw new CustomException("姓名長度必須在1-50個字元之間", HttpStatus.BAD_REQUEST);
        }
        if (!StringUtils.hasText(request.getEmail()) || !EMAIL_PATTERN.matcher(request.getEmail()).matches()
                || request.getEmail().length() > 100) {
            throw new CustomException("請輸入有效的電子郵件", HttpStatus.BAD_REQUEST);
        }
        if (!StringUtils.hasText(request.getPassword()) || request.getPassword().length() < 8
                || request.getPassword().length() > 255) {
            throw new CustomException("密碼長度必須在8-255個字元之間", HttpStatus.BAD_REQUEST);
        }
        if (StringUtils.hasText(request.getPhone()) && !PHONE_PATTERN.matcher(request.getPhone()).matches()) {
            throw new CustomException("請輸入有效的手機號碼", HttpStatus.BAD_REQUEST);
        }
    }

    private LoginResponse buildLoginResponse(User user, String token) {
        return LoginResponse.builder()
                .token(token)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .emailVerified(user.isEmailVerified())
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .build();
    }

    private UserResponse buildUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .birthday(user.getBirthday())
                .gender(user.getGender())
                .role(user.getRole())
                .address(user.getAddress())
                .avatar(user.getAvatar())
                .active(user.isActive())
                .emailVerified(user.isEmailVerified())
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .version(user.getVersion())
                .build();
    }
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email.toLowerCase().trim());
    }

    @Transactional
    public void logout(String token) {
        try {
            if (token != null) {
                // 使 JWT token 失效
                jwtTokenProvider.invalidateToken(token);
                log.info("User logged out successfully");
            }
        } catch (Exception e) {
            log.error("Logout failed", e);
            throw new CustomException("登出失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
