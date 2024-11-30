package org.example._citizencard3.service;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.dto.request.UpdateProfileRequest;
import org.example._citizencard3.dto.response.UserResponse;
import org.example._citizencard3.dto.response.UserStatsResponse;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.model.User;
import org.example._citizencard3.model.enums.UserRole;
import org.example._citizencard3.repositroy.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 獲取當前用戶資料
    public UserResponse getCurrentUserProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = findUserByEmail(email);
        return convertToResponse(user);
    }

    // 更新用戶資料
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = findUserByEmail(email);

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setBirthday(request.getBirthday());
        user.setGender(request.getGender());
        user.setAddress(request.getAddress());

        user = userRepository.save(user);
        return convertToResponse(user);
    }

    // 獲取所有用戶列表
    public List<UserResponse> getAllUsers(int page, int size, String search) {
        if (search != null && !search.isEmpty()) {
            return userRepository.findByNameContainingOrEmailContaining(search, search, PageRequest.of(page, size))
                    .stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        }
        return userRepository.findAll(PageRequest.of(page, size))
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 獲取指定用戶
    public UserResponse getUserById(Long id) {
        User user = findUserById(id);
        return convertToResponse(user);
    }

    // 更新用戶狀態
    @Transactional
    public void updateUserStatus(Long id, boolean active) {
        User user = findUserById(id);
        if (active) {
            user.activate();
        } else {
            user.deactivate();
        }
        userRepository.save(user);
    }

    // 更新用戶角色
    @Transactional
    public void updateUserRole(Long id, String role) {
        User user = findUserById(id);
        try {
            UserRole newRole = UserRole.valueOf(role);
            user.setRole(newRole);
            userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new CustomException("無效的角色類型", HttpStatus.BAD_REQUEST);
        }
    }

    // 刪除用戶
    @Transactional
    public void deleteUser(Long id) {
        User user = findUserById(id);
        user.deactivate();
        userRepository.save(user);
    }

    // 獲取用戶統計資料
    public UserStatsResponse getUserStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActiveTrue();
        long newUsersToday = userRepository.countByCreatedAtAfter(
                java.time.LocalDateTime.now().minusDays(1)
        );

        return UserStatsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .newUsersToday(newUsersToday)
                .build();
    }

    // 輔助方法：根據ID查找用戶
    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(
                        "找不到指定用戶",
                        HttpStatus.NOT_FOUND
                ));
    }

    // 輔助方法：根據郵箱查找用戶
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        "找不到指定用戶",
                        HttpStatus.NOT_FOUND
                ));
    }

    // 輔助方法：轉換為響應對象
    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .birthday(user.getBirthday())
                .gender(user.getGender())
                .address(user.getAddress())
                .role(user.getRole().name())
                .avatar(user.getAvatar())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .wallet(user.getWallet() != null ?
                        UserResponse.WalletInfo.builder()
                                .id(user.getWallet().getId())
                                .balance(user.getWallet().getBalance())
                                .build() : null)
                .build();
    }
}